$(document).ready(function () {
    const boardId = $('body').data('board-id');

    // DÒNG DEBUG: In giá trị boardId ra console ngay khi trang được tải.
    console.log("Board ID đã được đọc từ thẻ body:", boardId);

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
        let assigneesHtml = '';
        if (cardDto.assignees && cardDto.assignees.length > 0) {
            assigneesHtml = cardDto.assignees.map(assignee =>
                `<div class="member-avatar" title="${assignee.username}">${assignee.username.substring(0, 1).toUpperCase()}</div>`
            ).join('');
        }
        return `
            <div class="card" data-card-id="${cardDto.id}">
                <span>${cardDto.title}</span>
                <button class="card-delete-btn">×</button>
                <div class="card-assignees">${assigneesHtml}</div>
            </div>
        `;
    }

    function updateCardAssigneesView(cardId) {
        $.ajax({
            type: 'GET',
            url: `/api/cards/${cardId}`,
            success: function (cardDto) {
                let cardElement = $(`.card[data-card-id="${cardId}"]`);
                let assigneesContainer = cardElement.find('.card-assignees');
                assigneesContainer.empty();

                if (cardDto.assignees && cardDto.assignees.length > 0) {
                    cardDto.assignees.forEach(function (assignee) {
                        let avatar = `<div class="member-avatar" title="${assignee.username}">${assignee.username.substring(0, 1).toUpperCase()}</div>`;
                        assigneesContainer.append(avatar);
                    });
                }
            }
        });
    }

    function moveCardApi(cardId, targetListId, newPosition) {
        $.ajax({
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            type: 'PUT',
            url: `/api/cards/${cardId}/move`,
            data: JSON.stringify({targetListId: targetListId, newPosition: newPosition}),
            success: function () {
                console.log(`Card ${cardId} moved successfully.`);
            },
            error: function () {
                alert('Failed to move the card. Please refresh.');
            }
        });
    }

    const cardSortableOptions = {
        items: ".card",
        placeholder: "sortable-placeholder",
        cursor: "move",
        connectWith: ".card-container",
        stop: function (event, ui) {
            let cardId = ui.item.data('card-id');
            let targetListId = ui.item.closest('.card-list').data('list-id');
            let newPosition = ui.item.index();
            moveCardApi(cardId, targetListId, newPosition);
        }
    };

    $('.card-container').sortable(cardSortableOptions).disableSelection();

    $('#board-container').sortable({
        items: ".card-list",
        placeholder: "list-placeholder",
        cursor: "move",
        cancel: ".list-title, input, button, textarea, .card-container",
        update: function (event, ui) {
            let listOrder = $(this).sortable('toArray', {attribute: 'data-list-id'}).map(id => parseInt(id, 10));
            $.ajax({
                headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
                type: 'PUT',
                url: '/api/lists/updatePositions',
                data: JSON.stringify(listOrder),
                success: function () {
                    console.log('List positions updated.');
                },
                error: function () {
                    alert('Failed to save list order.');
                }
            });
        }
    });

    $('#add-list-form').on('submit', function (event) {
        event.preventDefault();
        let listTitle = $('#new-list-title').val().trim();
        if (!listTitle) return;
        $.ajax({
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            type: 'POST',
            url: `/api/lists?boardId=${boardId}`,
            data: JSON.stringify({title: listTitle}),
            success: function (responseDto) {
                let newListElement = $(createListHtml(responseDto));
                $('.add-list-wrapper').before(newListElement);
                newListElement.find('.card-container').sortable(cardSortableOptions).disableSelection();
                $('#new-list-title').val('');
            },
            error: function (error) {
                alert('An error occurred while creating the list.');
            }
        });
    });

    $(document).on('submit', '.add-card-form', function (event) {
        event.preventDefault();
        let form = $(this);
        let cardTitleInput = form.find('.new-card-title');
        let cardTitle = cardTitleInput.val().trim();
        if (!cardTitle) return;
        let listId = form.closest('.card-list').data('list-id');
        $.ajax({
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            type: 'POST',
            url: `/api/cards?listId=${listId}`,
            data: JSON.stringify({title: cardTitle}),
            success: function (responseDto) {
                let newCardElement = createCardHtml(responseDto);
                form.closest('.card-list').find('.card-container').append(newCardElement);
                cardTitleInput.val('');
            },
            error: function (error) {
                alert('An error occurred while creating the card.');
            }
        });
    });

    $('#invite-member-form').on('submit', function (event) {
        event.preventDefault();
        let emailToInvite = $('#invite-email').val().trim();
        if (!emailToInvite) return;
        $.ajax({
            type: 'POST',
            url: `/api/boards/${boardId}/members?email=${encodeURIComponent(emailToInvite)}`,
            success: function (response) {
                alert("Invitation sent successfully!");
                location.reload();
            },
            error: function (xhr) {
                alert('Error: ' + xhr.responseText);
            }
        });
    });

    $(document).on('focus', '.list-title', function () {
        $(this).data('original-title', $(this).text().trim());
    }).on('blur keypress', '.list-title', function (e) {
        if (e.type === 'keypress' && e.which !== 13) {
            return;
        }
        e.preventDefault();

        let listTitleElement = $(this);
        let originalTitle = listTitleElement.data('original-title');
        let newTitle = listTitleElement.text().trim();

        if (listTitleElement.is(':focus')) {
            listTitleElement.blur();
        }

        if (!newTitle || newTitle === originalTitle) {
            listTitleElement.text(originalTitle);
            return;
        }

        let listId = listTitleElement.closest('.card-list').data('list-id');
        $.ajax({
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            type: 'PUT',
            url: `/api/lists/${listId}`,
            data: JSON.stringify({title: newTitle}),
            success: function (responseDto) {
                listTitleElement.text(responseDto.title);
            },
            error: function () {
                alert('Could not update the list title.');
                listTitleElement.text(originalTitle);
            }
        });
    });

    $(document).on('click', '.list-delete-btn', function () {
        let listElement = $(this).closest('.card-list');
        if (!confirm('Are you sure you want to delete this list and all its cards?')) return;
        $.ajax({
            type: 'DELETE',
            url: `/api/lists/${listElement.data('list-id')}`,
            success: function () {
                listElement.fadeOut(300, function () {
                    $(this).remove();
                });
            },
            error: function () {
                alert('Could not delete the list.');
            }
        });
    });

    $(document).on('click', '.card', function (event) {
        if ($(event.target).is('.card-delete-btn')) {
            return;
        }
        let cardId = $(this).data('card-id');
        $('#card-modal').data('current-card-id', cardId);

        // DÒNG DEBUG: Kiểm tra boardId ngay trước khi gọi API
        console.log("Kiểm tra Board ID ngay trước lời gọi AJAX:", boardId);

        $.when(
            $.ajax({type: 'GET', url: `/api/cards/${cardId}`}),
            $.ajax({type: 'GET', url: `/api/boards/${boardId}/members`})
        ).done(function (cardResponse, boardMembersResponse) {
            let cardDto = cardResponse[0];
            let boardMembers = boardMembersResponse[0];

            $('#modal-card-title').text(cardDto.title);
            $('#modal-card-description').val(cardDto.description || '');

            let memberList = $('#modal-member-list');
            memberList.empty();

            let assignedUserIds = cardDto.assignees.map(user => user.id);

            boardMembers.forEach(function (member) {
                let isChecked = assignedUserIds.includes(member.id);
                let memberItem = `
                    <li class="member-list-item">
                        <label>
                            <input type="checkbox" class="assignee-checkbox" data-user-id="${member.id}" ${isChecked ? 'checked' : ''}>
                            <div class="member-avatar" title="${member.username}">${member.username.substring(0, 1).toUpperCase()}</div>
                            <span>${member.username} (${member.email})</span>
                        </label>
                    </li>`;
                memberList.append(memberItem);
            });

            $('#card-modal').show();
        }).fail(function (jqXHR, textStatus, errorThrown) {
            // Hiển thị lỗi chi tiết hơn
            console.error("Lỗi khi lấy chi tiết card hoặc members:", jqXHR.status, jqXHR.responseText);
            alert('Could not fetch card details. Check console for more info.');
        });
    });

    $('.close-modal, .modal-overlay').on('click', function (event) {
        if (this === event.target) {
            $('#card-modal').hide();
        }
    });

    $('#save-card-details').on('click', function () {
        let cardId = $('#card-modal').data('current-card-id');
        if (!cardId) return;

        let updatedTitle = $('#modal-card-title').text().trim();
        if (!updatedTitle) {
            alert('Card title cannot be empty.');
            return;
        }
        let updatedDescription = $('#modal-card-description').val().trim();

        $.ajax({
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            type: 'PUT',
            url: `/api/cards/${cardId}`,
            data: JSON.stringify({title: updatedTitle, description: updatedDescription}),
            success: function () {
                console.log('Card details saved successfully.');
                $(`.card[data-card-id="${cardId}"] > span`).text(updatedTitle);
                $('#card-modal').hide();
            },
            fail: function () {
                alert('An error occurred while saving. Please try again.');
            }
        });
    });

    $(document).on('change', '.assignee-checkbox', function () {
        let checkbox = $(this);
        let cardId = $('#card-modal').data('current-card-id');
        let userId = checkbox.data('user-id');

        let apiUrl = `/api/cards/${cardId}/assignees/${userId}`;
        let apiType = checkbox.is(':checked') ? 'POST' : 'DELETE';

        $.ajax({
            type: apiType,
            url: apiUrl,
            success: function () {
                console.log('Assignee updated.');
                updateCardAssigneesView(cardId);
            },
            error: function () {
                alert('Failed to update assignee.');
                checkbox.prop('checked', !checkbox.is(':checked'));
            }
        });
    });

    function deleteCard(cardId) {
        if (!confirm('Are you sure you want to permanently delete this card?')) return;
        $.ajax({
            type: 'DELETE',
            url: `/api/cards/${cardId}`,
            success: function () {
                $(`.card[data-card-id="${cardId}"]`).fadeOut(300, function () {
                    $(this).remove();
                });
                if ($('#card-modal').data('current-card-id') == cardId) {
                    $('#card-modal').hide();
                }
            },
            error: function () {
                alert('Could not delete the card.');
            }
        });
    }

    $(document).on('click', '.card-delete-btn', function (event) {
        event.stopPropagation();
        deleteCard($(this).closest('.card').data('card-id'));
    });

    $('#delete-card-from-modal').on('click', function () {
        let cardId = $('#card-modal').data('current-card-id');
        if (cardId) deleteCard(cardId);
    });

    $(document).on('focus', '#modal-card-title', function () {
        $(this).data('original-title', $(this).text().trim());
    }).on('blur keypress', '#modal-card-title', function (e) {
        if (e.type === 'keypress' && e.which !== 13) {
            return;
        }
        e.preventDefault();
        $(this).blur();
    });
});