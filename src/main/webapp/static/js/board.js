$(document).ready(function () {
    // Check if jQuery is available
    if (typeof $ === 'undefined') {
        console.error('jQuery is not loaded - board functionality will be limited');
        return;
    }
    
    // Check if jQuery UI is available
    if (typeof $.ui === 'undefined') {
        console.warn('jQuery UI is not loaded - drag and drop functionality will be limited');
    }
    
    // Check if SockJS and STOMP are available
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.warn('SockJS or STOMP not loaded - WebSocket functionality will be limited');
    }
    
    // Check if WebSocketManager is available
    if (typeof window.webSocketManager === 'undefined') {
        console.warn('WebSocketManager not available - real-time features will be disabled');
    }
    
    const boardId = $("body").data("board-id");
    let allLabels = [];
    
    // Get current username from authentication
    const username = $("body").data("username") || "anonymous";

    
    // Initialize WebSocket connection
    if (window.webSocketManager && typeof window.webSocketManager.connect === 'function') {
        window.webSocketManager.connect(boardId, username);
        
        // Don't set initial status - let WebSocket handle it when user actually joins
    } else {
        console.warn('WebSocketManager not available - WebSocket features will be disabled');
    }

    // Send leave message when page is about to unload
    $(window).on('beforeunload', function() {
        if (window.webSocketManager && window.webSocketManager.isConnected) {
            window.webSocketManager.disconnect();
        }
    });




    function fetchAllLabels() {
        $.ajax({
            type: "GET",
            url: "/api/labels",
            success: function (labels) {
                allLabels = labels;
            },
            error: function (xhr, status, error) {
                console.error("Could not load labels:", error);
            }
        });
    }

    fetchAllLabels();

    // Add List functionality
    $(document).on('submit', '.add-list-form', function(e) {
        e.preventDefault();
        const title = $(this).find('#new-list-title').val().trim();
        if (title) {
            $.ajax({
                type: "POST",
                url: "/api/lists?boardId=" + boardId,
                contentType: "application/json",
                data: JSON.stringify({
                    title: title
                }),
                success: function(listDto) {
                    // Don't add list here - let WebSocket handle it
                    // Just clear the input field
                    $(e.target).find('#new-list-title').val('');
                },
                error: function() {
                    alert("Could not create list.");
                }
            });
        }
    });

    // Add Card functionality
    $(document).on('submit', '.add-card-form', function(e) {
        e.preventDefault();
        const title = $(this).find('.new-card-title').val().trim();
        const listId = $(this).closest('.card-list').data('list-id');
        if (title && listId) {
            $.ajax({
                type: "POST",
                url: "/api/cards?listId=" + listId,
                contentType: "application/json",
                data: JSON.stringify({
                    title: title
                }),
                success: function(cardDto) {
                    // Don't add card here - let WebSocket handle it
                    // Just clear the input field
                    $(e.target).find('.new-card-title').val('');
                },
                error: function() {
                    alert("Could not create card.");
                }
            });
        }
    });

    // Edit List Title functionality
    $(document).on('blur', '.list-title', function() {
        const listId = $(this).closest('.card-list').data('list-id');
        const newTitle = $(this).text().trim();
        if (newTitle && listId) {
            $.ajax({
                type: "PUT",
                url: "/api/lists/" + listId,
                contentType: "application/json",
                data: JSON.stringify({
                    title: newTitle
                }),
                success: function() {
                    // Title updated successfully
                },
                error: function() {
                    alert("Could not update list title.");
                }
            });
        }
    });

    // Card Click functionality
    $(document).on("click", ".card", function (event) {
        if ($(event.target).is(".card-delete-btn") || $(event.target).is(".card-label") || $(event.target).is(".member-avatar")) {
            return;
        }
        let cardId = $(this).data("card-id");
        $("#card-modal").data("card-id", cardId);

        $("#file-upload-input").val("");
        $("#file-name-display").text("");
        $("#upload-file-button").hide();

        $.when(
            $.ajax({type: "GET", url: `/api/cards/${cardId}`}),
            $.ajax({type: "GET", url: `/api/boards/${boardId}/members`}),
            $.ajax({type: "GET", url: `/api/cards/${cardId}/attachments`})
        )
            .done(function (cardResponse, boardMembersResponse, attachmentsResponse) {
                let cardDto = cardResponse[0];
                let boardMembers = boardMembersResponse[0];
                let attachments = attachmentsResponse[0];

                $("#modal-card-title").text(cardDto.title);
                $("#modal-card-description").val(cardDto.description || "");

                let memberList = $("#modal-member-list");
                memberList.empty();

                let assignedUserIds = (cardDto.assignees || []).map((user) => user.id);

                boardMembers.forEach(function (member) {
                    let isChecked = assignedUserIds.includes(member.id);
                    let memberItem = `
                    <li class="member-list-item">
                        <label>
                            <input type="checkbox" class="assignee-checkbox" data-user-id="${
                        member.id
                    }" ${isChecked ? "checked" : ""}>
                            <div class="member-avatar" data-username="${member.username}" title="${
                        member.username
                    }">
                        ${member.username
                        .substring(0, 1)
                        .toUpperCase()}
                    </div>
                            <span>${member.username} (${member.email})</span>
                        </label>
                    </li>`;
                    memberList.append(memberItem);
                });

                populateAttachmentList(attachments);
                populateLabelsInModal(cardDto.labels);
                $("#card-modal").show();
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.error(
                    "Lỗi khi lấy chi tiết card hoặc members:",
                    jqXHR.status,
                    jqXHR.responseText
                );
                alert("Could not fetch card details. Check console for more info.");
            });
    });



    function updateCardAssigneesView(cardId) {
        $.ajax({
            type: "GET",
            url: `/api/cards/${cardId}`,
            success: function (cardDto) {
                // Use WebSocketManager's createCardHtml method
                if (window.webSocketManager && typeof window.webSocketManager.createCardHtml === 'function') {
                    const newCardHtml = window.webSocketManager.createCardHtml(cardDto);
                    $(`.card[data-card-id="${cardId}"]`).replaceWith(newCardHtml);
                } else {
                    console.error('WebSocketManager.createCardHtml not available');
                }
            },
        });
    }

    function moveCardApi(cardId, targetListId, newPosition) {
        $.ajax({
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            type: "PUT",
            url: `/api/cards/${cardId}/move`,
            data: JSON.stringify({
                targetListId: targetListId,
                newPosition: newPosition,
            }),
            success: function () {
            },
            error: function (xhr) {
                console.error("Error moving card:", xhr.status, xhr.responseText);
                if (xhr.status === 403) {
                    alert("Bạn không có quyền di chuyển card này. Chỉ có chủ sở hữu board hoặc thành viên mới có thể di chuyển card.");
                } else if (xhr.status === 404) {
                    alert("Card hoặc list không tồn tại.");
                } else {
                    alert("Lỗi khi di chuyển card: " + (xhr.responseText || "Unknown error"));
                }
            },
        });
    }

    const cardSortableOptions = {
        items: ".card",
        placeholder: "sortable-placeholder",
        cursor: "move",
        connectWith: ".card-container",
        stop: function (event, ui) {
            let cardId = ui.item.data("card-id");
            let targetListId = ui.item.closest(".card-list").data("list-id");
            let newPosition = ui.item.index();
            moveCardApi(cardId, targetListId, newPosition);
        },
    };

    $(".card-container").sortable(cardSortableOptions).disableSelection();

    $("#board-container").sortable({
        items: ".card-list",
        placeholder: "list-placeholder",
        cursor: "move",
        cancel: ".list-title, input, button, textarea, .card-container",
        start: function(event, ui) {
        },
        update: function (event, ui) {
            let listOrder = $(this)
                .sortable("toArray", {attribute: "data-list-id"})
                .map((id) => parseInt(id, 10));
            
            $.ajax({
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
                type: "PUT",
                url: "/api/lists/updatePositions",
                data: JSON.stringify(listOrder),
                success: function () {
                    // WebSocket will handle real-time updates for other users
                },
                error: function (xhr) {
                    console.error("Failed to save list order:", xhr);
                    alert("Failed to save list order.");
                },
            });
        },
    });



    $("#invite-member-form").on("submit", function (event) {
        event.preventDefault();
        let emailToInvite = $("#invite-email").val().trim();
        if (!emailToInvite) return;
        $.ajax({
            type: "POST",
            url: `/api/boards/${boardId}/members?email=${encodeURIComponent(
                emailToInvite
            )}`,
            success: function (response) {
                alert("Invitation sent successfully!");
                location.reload();
            },
            error: function (xhr) {
                alert("Error: " + xhr.responseText);
            },
        });
    });

    $(document)
        .on("focus", ".list-title", function () {
            $(this).data("original-title", $(this).text().trim());
        })
        .on("blur keypress", ".list-title", function (e) {
            if (e.type === "keypress" && e.which !== 13) {
                return;
            }
            e.preventDefault();

            let listTitleElement = $(this);
            let originalTitle = listTitleElement.data("original-title");
            let newTitle = listTitleElement.text().trim();

            if (listTitleElement.is(":focus")) {
                listTitleElement.blur();
            }

            if (!newTitle || newTitle === originalTitle) {
                listTitleElement.text(originalTitle);
                return;
            }

            let listId = listTitleElement.closest(".card-list").data("list-id");
            $.ajax({
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
                type: "PUT",
                url: `/api/lists/${listId}`,
                data: JSON.stringify({title: newTitle}),
                success: function (responseDto) {
                    listTitleElement.text(responseDto.title);
                    // WebSocket will handle real-time updates for other users
                },
                error: function () {
                    alert("Could not update the list title.");
                    listTitleElement.text(originalTitle);
                },
            });
        });

    $(document).on("click", ".list-delete-btn", function () {
        let listElement = $(this).closest(".card-list");
        if (
            !confirm("Are you sure you want to delete this list and all its cards?")
        )
            return;
        $.ajax({
            type: "DELETE",
            url: `/api/lists/${listElement.data("list-id")}`,
            success: function () {
                listElement.fadeOut(300, function () {
                    $(this).remove();
                });
                // WebSocket will handle real-time updates for other users
            },
            error: function () {
                alert("Could not delete the list.");
            },
        });
    });

    $(".close-modal, .modal-overlay").on("click", function (event) {
        if (this === event.target) {
            $("#card-modal").hide();
        }
    });

    $(document).on("click", "#save-card-details", function (e) {
        e.preventDefault();
        let cardId = $("#card-modal").data("card-id");
        if (!cardId) {
            console.error("No card ID found in modal");
            alert("No card selected.");
            return;
        }

        let updatedTitle = $("#modal-card-title").text().trim();
        if (!updatedTitle) {
            alert("Card title cannot be empty.");
            return;
        }
        let updatedDescription = $("#modal-card-description").val().trim();
        $.ajax({
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            type: "PUT",
            url: `/api/cards/${cardId}`,
            data: JSON.stringify({
                title: updatedTitle,
                description: updatedDescription,
            }),
            success: function (response) {
                $(`.card[data-card-id="${cardId}"] > span`).text(updatedTitle);
                $("#card-modal").hide();
                
                // WebSocket will handle real-time updates for other users
            },
            error: function (xhr) {
                console.error("Error saving card details:", xhr.status, xhr.responseText);
                alert("An error occurred while saving. Please try again.");
            },
        });
    });

    $(document).on("change", ".assignee-checkbox", function () {
        let checkbox = $(this);
        let cardId = $("#card-modal").data("card-id");
        let userId = checkbox.data("user-id");

        let apiUrl = `/api/cards/${cardId}/assignees/${userId}`;
        let apiType = checkbox.is(":checked") ? "POST" : "DELETE";

        $.ajax({
            type: apiType,
            url: apiUrl,
            success: function () {
                updateCardAssigneesView(cardId);
                // WebSocket will handle real-time updates for other users
            },
            error: function () {
                alert("Failed to update assignee.");
                checkbox.prop("checked", !checkbox.is(":checked"));
            },
        });
    });

    function deleteCard(cardId) {
        if (!confirm("Are you sure you want to permanently delete this card?"))
            return;
        $.ajax({
            type: "DELETE",
            url: `/api/cards/${cardId}`,
            success: function () {
                $(`.card[data-card-id="${cardId}"]`).fadeOut(300, function () {
                    $(this).remove();
                });
                if ($("#card-modal").data("card-id") == cardId) {
                    $("#card-modal").hide();
                }
                // WebSocket will handle real-time updates for other users
            },
            error: function () {
                alert("Could not delete the card.");
            },
        });
    }

    $(document).on("click", ".card-delete-btn", function (event) {
        event.stopPropagation();
        deleteCard($(this).closest(".card").data("card-id"));
    });

    $("#delete-card-from-modal").on("click", function () {
        let cardId = $("#card-modal").data("card-id");
        if (cardId) deleteCard(cardId);
    });

    $(document)
        .on("focus", "#modal-card-title", function () {
            $(this).data("original-title", $(this).text().trim());
        })
        .on("blur keypress", "#modal-card-title", function (e) {
            if (e.type === "keypress" && e.which !== 13) {
                return;
            }
            e.preventDefault();
            $(this).blur();
        });

    $(document).on("click", ".remove-member-btn", function (event) {
        event.stopPropagation();

        const button = $(this);
        const userId = button.data("user-id");
        const boardId = $("body").data("board-id");

        if (
            !confirm(
                "Are you sure you want to remove this member? This will also un-assign them from all cards."
            )
        ) {
            return;
        }

        $.ajax({
            type: "DELETE",
            url: `/api/boards/${boardId}/members/${userId}`,
            success: function (response) {
                alert(response);
                location.reload();
            },
            error: function (xhr) {
                alert("Error: " + xhr.responseText);
            },
        });
    });

    function populateAttachmentList(attachments) {
        const listElement = $("#attachment-list");
        listElement.empty();
        if (attachments && attachments.length > 0) {
            attachments.forEach((att) => {
                addAttachmentToUI(att);
            });
        }
    }

    function addAttachmentToUI(attachment) {
        const listElement = $("#attachment-list");
        const attachmentHtml = `
        <li class="attachment-list-item" data-attachment-id="${attachment.id}">
            <a href="${attachment.url}" target="_blank" class="attachment-link">
                <i class="fas fa-file"></i>
                <span>${attachment.fileName}</span>
            </a>
            <button class="delete-attachment-btn" title="Delete attachment">
                <i class="fas fa-trash-alt"></i>
            </button>
        </li>
    `;
        listElement.append(attachmentHtml);
    }

    $("#select-file-button").on("click", function () {
        $("#file-upload-input").click();
    });

    $("#file-upload-input").on("change", function () {
        const fileInput = $(this)[0];
        if (fileInput.files.length > 0) {
            const fileName = fileInput.files[0].name;
            $("#file-name-display").text(fileName);
            $("#upload-file-button").show();
        } else {
            $("#file-name-display").text("");
            $("#upload-file-button").hide();
        }
    });

    $("#upload-file-button").on("click", function () {
        const fileInput = $("#file-upload-input")[0];
        const cardId = $("#card-modal").data("card-id");

        if (fileInput.files.length === 0) {
            alert("Please choose a file first.");
            return;
        }

        const file = fileInput.files[0];
        const formData = new FormData();
        formData.append("file", file);
        formData.append("cardId", cardId);

        const uploadButton = $(this);
        uploadButton.prop("disabled", true).text("Uploading...");

        $.ajax({
            type: "POST",
            url: "/api/attachments/upload",
            data: formData,
            processData: false,
            contentType: false,
            success: function (newAttachment) {
                addAttachmentToUI(newAttachment);
                $("#file-upload-input").val("");
                $("#file-name-display").text("");
                uploadButton.hide();
            },
            error: function (xhr) {
                alert("Error uploading file: " + xhr.responseText);
            },
            complete: function () {
                uploadButton.prop("disabled", false).text("Upload");
            },
        });
    });
    
    $(document).on("click", ".delete-attachment-btn", function () {
        const button = $(this);
        const attachmentItem = button.closest(".attachment-list-item");
        const attachmentId = attachmentItem.data("attachment-id");

        if (!confirm("Are you sure you want to delete this attachment?")) {
            return;
        }

        $.ajax({
            type: "DELETE",
            url: `/api/attachments/${attachmentId}`,
            success: function () {
                attachmentItem.fadeOut(300, function () {
                    $(this).remove();
                });
            },
            error: function (xhr) {
                alert("Error deleting file: " + xhr.responseText);
            },
        });
    });

    function updateCardLabelsView(cardId, labels) {
        const cardElement = $(`.card[data-card-id="${cardId}"]`);
        if (cardElement.length === 0) return;
        
        const labelsContainer = cardElement.find('.card-labels');
        labelsContainer.empty();
        
        if (!labels) return;
        
        let labelsArray = [];
        if (Array.isArray(labels)) {
            labelsArray = labels;
        } else if (typeof labels === 'object') {
            labelsArray = Array.from(labels);
        }
        
        if (labelsArray.length > 0) {
            const labelsHtml = labelsArray.map(label =>
                `<div class="card-label" style="background-color: ${label.color};" title="${label.name}">${label.name}</div>`
            ).join('');
            labelsContainer.html(labelsHtml);
        }
    }

    function populateLabelsInModal(assignedLabels) {
        const listElement = $("#modal-label-list");
        listElement.empty();
        
        // Handle null/undefined assignedLabels
        if (!assignedLabels) {
            assignedLabels = [];
        }
        
        let labelsArray = [];
        if (Array.isArray(assignedLabels)) {
            labelsArray = assignedLabels;
        } else if (typeof assignedLabels === 'object') {
            labelsArray = Array.from(assignedLabels);
        } else {
            labelsArray = [];
        }
        
        const assignedLabelIds = new Set(labelsArray.map(l => l.id));

        if (allLabels.length === 0) {
            return;
        }

        allLabels.forEach(label => {
            const isSelected = assignedLabelIds.has(label.id);
            const itemHtml = `
            <li class="label-list-item ${isSelected ? 'selected' : ''}" 
                style="background-color: ${label.color};"
                data-label-id="${label.id}">
                <span>${label.name}</span>
                <i class="fas fa-check"></i>
            </li>
        `;
            listElement.append(itemHtml);
        });
    }

    $(document).on('click', '.label-list-item', function () {
        const labelItem = $(this);
        const cardId = $("#card-modal").data("card-id");
        const labelId = labelItem.data("label-id");
        const isSelected = labelItem.hasClass('selected');

        if (!cardId) {
            console.error('No card ID found in modal');
            return;
        }

        const apiUrl = `/api/cards/${cardId}/labels/${labelId}`;
        const apiType = isSelected ? "DELETE" : "POST";

        $.ajax({
            type: apiType,
            url: apiUrl,
            success: function () {
                labelItem.toggleClass('selected');
                $.get(`/api/cards/${cardId}`, function (cardDto) {
                    updateCardLabelsView(cardId, cardDto.labels);
                });
                // WebSocket will handle real-time updates for other users
            },
            error: function (xhr, status, error) {
                console.error('Failed to update label:', error);
                alert('Failed to update label: ' + error);
            }
        });
    });
});
