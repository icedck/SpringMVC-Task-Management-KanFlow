$(document).ready(function () {
    const boardId = $("body").data("board-id");
    let allLabels = [];

    console.log("Board ID đã được đọc từ thẻ body:", boardId);

    function fetchAllLabels() {
        $.ajax({
            type: "GET",
            url: "/api/labels",
            success: function (labels) {
                allLabels = labels;
            },
            error: function () {
                console.error("Could not load labels.");
            }
        });
    }

    fetchAllLabels();

    function createListHtml(listDto) {
        return `
            <div class="card-list" data-list-id="${listDto.id}">
                <div class="card-list-header">
                    <div class="list-title" contenteditable="true">${listDto.title}</div>
                    <button class="list-delete-btn">×</button>
                </div>
                <div class="card-container"></div>
                <div class="add-card-form-container">
                    <form class="add-card-form">
                        <input type="text" class="new-card-title" placeholder="Enter a title for this card..." required />
                        <button type="submit">Add Card</button>
                    </form>
                </div>
            </div>
        `;
    }

    function createCardHtml(cardDto) {
        let assigneesHtml = cardDto.assignees?.map(assignee =>
            `<div class="member-avatar" title="${assignee.username}">${assignee.username.substring(0, 1).toUpperCase()}</div>`
        ).join("") || "";

        let labelsHtml = cardDto.labels?.map(label =>
            `<div class="card-label" style="background-color: ${label.color};" title="${label.name}">${label.name}</div>`
        ).join('') || "";

        return `
        <div class="card" data-card-id="${cardDto.id}">
            <div class="card-labels">${labelsHtml}</div>
            <span>${cardDto.title}</span>
            <button class="card-delete-btn">×</button>
            <div class="card-assignees">${assigneesHtml}</div>
        </div>
    `;
    }

    function updateCardAssigneesView(cardId) {
        $.ajax({
            type: "GET",
            url: `/api/cards/${cardId}`,
            success: function (cardDto) {
                const newCardHtml = createCardHtml(cardDto);
                $(`.card[data-card-id="${cardId}"]`).replaceWith(newCardHtml);
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
                console.log(`Card ${cardId} moved successfully.`);
            },
            error: function () {
                alert("Failed to move the card. Please refresh.");
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
                    console.log("List positions updated.");
                },
                error: function () {
                    alert("Failed to save list order.");
                },
            });
        },
    });

    $("#add-list-form").on("submit", function (event) {
        event.preventDefault();
        let listTitle = $("#new-list-title").val().trim();
        if (!listTitle) return;
        $.ajax({
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            type: "POST",
            url: `/api/lists?boardId=${boardId}`,
            data: JSON.stringify({title: listTitle}),
            success: function (responseDto) {
                let newListElement = $(createListHtml(responseDto));
                $(".add-list-wrapper").before(newListElement);
                newListElement
                    .find(".card-container")
                    .sortable(cardSortableOptions)
                    .disableSelection();
                $("#new-list-title").val("");
            },
            error: function (error) {
                alert("An error occurred while creating the list.");
            },
        });
    });

    $(document).on("submit", ".add-card-form", function (event) {
        event.preventDefault();
        let form = $(this);
        let cardTitleInput = form.find(".new-card-title");
        let cardTitle = cardTitleInput.val().trim();
        if (!cardTitle) return;
        let listId = form.closest(".card-list").data("list-id");
        $.ajax({
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            type: "POST",
            url: `/api/cards?listId=${listId}`,
            data: JSON.stringify({title: cardTitle}),
            success: function (responseDto) {
                let newCardElement = createCardHtml(responseDto);
                form
                    .closest(".card-list")
                    .find(".card-container")
                    .append(newCardElement);
                cardTitleInput.val("");
            },
            error: function (error) {
                alert("An error occurred while creating the card.");
            },
        });
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
            },
            error: function () {
                alert("Could not delete the list.");
            },
        });
    });

    $(document).on("click", ".card", function (event) {
        if ($(event.target).is(".card-delete-btn")) {
            return;
        }
        let cardId = $(this).data("card-id");
        $("#card-modal").data("current-card-id", cardId);

        console.log("Kiểm tra Board ID ngay trước lời gọi AJAX:", boardId);

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

                let assignedUserIds = cardDto.assignees.map((user) => user.id);

                boardMembers.forEach(function (member) {
                    let isChecked = assignedUserIds.includes(member.id);
                    let memberItem = `
                    <li class="member-list-item">
                        <label>
                            <input type="checkbox" class="assignee-checkbox" data-user-id="${
                        member.id
                    }" ${isChecked ? "checked" : ""}>
                            <div class="member-avatar" title="${
                        member.username
                    }">${member.username
                        .substring(0, 1)
                        .toUpperCase()}</div>
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

    $(".close-modal, .modal-overlay").on("click", function (event) {
        if (this === event.target) {
            $("#card-modal").hide();
        }
    });

    $("#save-card-details").on("click", function () {
        let cardId = $("#card-modal").data("current-card-id");
        if (!cardId) return;

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
            success: function () {
                console.log("Card details saved successfully.");
                $(`.card[data-card-id="${cardId}"] > span`).text(updatedTitle);
                $("#card-modal").hide();
            },
            fail: function () {
                alert("An error occurred while saving. Please try again.");
            },
        });
    });

    $(document).on("change", ".assignee-checkbox", function () {
        let checkbox = $(this);
        let cardId = $("#card-modal").data("current-card-id");
        let userId = checkbox.data("user-id");

        let apiUrl = `/api/cards/${cardId}/assignees/${userId}`;
        let apiType = checkbox.is(":checked") ? "POST" : "DELETE";

        $.ajax({
            type: apiType,
            url: apiUrl,
            success: function () {
                console.log("Assignee updated.");
                updateCardAssigneesView(cardId);
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
                if ($("#card-modal").data("current-card-id") == cardId) {
                    $("#card-modal").hide();
                }
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
        let cardId = $("#card-modal").data("current-card-id");
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
        const cardId = $("#card-modal").data("current-card-id");

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
        const labelsContainer = cardElement.find('.card-labels');
        labelsContainer.empty();
        if (labels && labels.length > 0) {
            const labelsHtml = labels.map(label =>
                `<div class="card-label" style="background-color: ${label.color};" title="${label.name}">${label.name}</div>`
            ).join('');
            labelsContainer.html(labelsHtml);
        }
    }

    function populateLabelsInModal(assignedLabels) {
        const listElement = $("#modal-label-list");
        listElement.empty();
        const assignedLabelIds = new Set(assignedLabels.map(l => l.id));

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
        const cardId = $("#card-modal").data("current-card-id");
        const labelId = labelItem.data("label-id");
        const isSelected = labelItem.hasClass('selected');

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
            },
            error: function () {
                alert('Failed to update label.');
            }
        });
    });
});
