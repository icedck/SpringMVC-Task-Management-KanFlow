class WebSocketManager {
    constructor() {
        this.socket = null;
        this.stompClient = null;
        this.boardId = null;
        this.username = null;
        this.isConnected = false;
        this.hasJoined = false;
    }

    connect(boardId, username) {
        this.boardId = boardId;
        this.username = username;
        this.hasJoined = false;
        
        
        // Check if SockJS and STOMP are available
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.error('SockJS or STOMP not loaded');
            return;
        }
        
        try {
            // Create SockJS connection
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);
            
            // Disable debug logging
            this.stompClient.debug = null;
            
            const self = this;
            
            this.stompClient.connect(
                { username: username },
                function(frame) {
                    self.isConnected = true;
                    self.subscribeToBoard();
                },
                function(error) {
                    console.error('WebSocket connection error:', error);
                    self.isConnected = false;
                    // Don't retry automatically to avoid infinite loops
                }
            );
        } catch (error) {
            console.error('Error creating WebSocket connection:', error);
            // Don't crash the page if WebSocket fails
            this.isConnected = false;
        }
    }

    subscribeToBoard() {
        if (!this.stompClient || !this.isConnected) {
            return;
        }
        
        const self = this;
        
        
        try {
            // Subscribe to board updates
            this.stompClient.subscribe('/topic/board/' + this.boardId, function(message) {
                try {
                    const data = JSON.parse(message.body);
                    self.handleBoardMessage(data);
                } catch (e) {
                    console.error('Error parsing board message:', e);
                    console.error('Raw message:', message.body);
                }
            });
            
            // Join the board (only once)
            if (!this.hasJoined) {
                try {
                    this.stompClient.send('/app/board/' + this.boardId + '/join', {}, this.username);
                    this.hasJoined = true;
                } catch (e) {
                    console.error('Error joining board:', e);
                }
            }
        } catch (error) {
            console.error('Error subscribing to board:', error);
        }
    }

    handleBoardMessage(message) {
        
        try {
            switch (message.type) {
                case 'CARD_UPDATE':
                    this.handleCardUpdate(message);
                    break;
                case 'CARD_MOVE':
                    this.handleCardMove(message);
                    break;
                case 'CARD_CREATE':
                    this.handleCardCreate(message);
                    break;
                case 'CARD_DELETE':
                    this.handleCardDelete(message);
                    break;
                case 'LIST_UPDATE':
                    this.handleListUpdate(message);
                    break;
                case 'LIST_MOVE':
                    this.handleListMove(message);
                    break;
                case 'LIST_CREATE':
                    this.handleListCreate(message);
                    break;
                case 'LIST_DELETE':
                    this.handleListDelete(message);
                    break;
                case 'MEMBER_JOIN':
                    this.handleMemberJoin(message);
                    break;
                case 'MEMBER_LEAVE':
                    this.handleMemberLeave(message);
                    break;
                case 'ATTACHMENT_UPDATE':
                    this.handleAttachmentUpdate(message);
                    break;
                default:
            }
        } catch (error) {
            console.error('Error handling board message:', error);
        }
    }

    handleListCreate(message) {
        if (typeof $ === 'undefined') {
            return;
        }
        
        if (message.listId && message.title) {
            const list = {
                id: message.listId,
                title: message.title,
                position: message.position || 0
            };
            
            const listHtml = this.createListHtml(list);
            $('.add-list-wrapper').before(listHtml);
            
            // Initialize sortable for the new list
            const newListElement = $(`[data-list-id="${list.id}"]`);
            newListElement.find('.card-container').sortable({
                connectWith: '.card-container',
                placeholder: 'sortable-placeholder',
                update: function(event, ui) {
                    // Handle card reordering
                }
            }).disableSelection();
        } else {
        }
    }

    createListHtml(list) {
        return `
            <div class="card-list" data-list-id="${list.id}">
                <div class="card-list-header">
                    <div class="list-title" contenteditable="true">${list.title}</div>
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

    createCardHtml(card) {
        // Handle labels
        let labelsHtml = "";
        if (card.labels) {
            if (Array.isArray(card.labels)) {
                labelsHtml = card.labels.map(label =>
                    `<div class="card-label" style="background-color: ${label.color};" title="${label.name}">${label.name}</div>`
                ).join('');
            } else if (typeof card.labels === 'object') {
                // Handle Set or other object types
                labelsHtml = Array.from(card.labels).map(label =>
                    `<div class="card-label" style="background-color: ${label.color};" title="${label.name}">${label.name}</div>`
                ).join('');
            }
        }

        // Handle assignees
        let assigneesHtml = "";
        if (card.assignees) {
            if (Array.isArray(card.assignees)) {
                assigneesHtml = card.assignees.map(assignee =>
                    `<div class="member-avatar" data-username="${assignee.username}" title="${assignee.username}">
                        ${assignee.username.substring(0, 1).toUpperCase()}
                    </div>`
                ).join("");
            } else if (typeof card.assignees === 'object') {
                assigneesHtml = Array.from(card.assignees).map(assignee =>
                    `<div class="member-avatar" data-username="${assignee.username}" title="${assignee.username}">
                        ${assignee.username.substring(0, 1).toUpperCase()}
                    </div>`
                ).join("");
            }
        }

        return `
            <div class="card" data-card-id="${card.id}">
                <div class="card-labels">${labelsHtml}</div>
                <span class="card-title">${card.title}</span>
                <button class="card-delete-btn">×</button>
                <div class="card-assignees">${assigneesHtml}</div>
            </div>
        `;
    }

    handleCardUpdate(message) {
        
        const cardElement = $(`.card[data-card-id="${message.cardId}"]`);
        if (cardElement.length > 0) {
            // Update card title if provided
            if (message.title) {
                // Find the span element that contains the card title (it doesn't have a class)
                const titleSpan = cardElement.find('span').first();
                if (titleSpan.length > 0) {
                    titleSpan.text(message.title);
                }
            }
            
            // Update labels
            if (message.labels !== undefined) {
                const labelsContainer = cardElement.find('.card-labels');
                labelsContainer.empty();
                
                if (message.labels) {
                    // Handle both Array and Set/Object
                    let labelsArray = [];
                    if (Array.isArray(message.labels)) {
                        labelsArray = message.labels;
                    } else if (typeof message.labels === 'object') {
                        labelsArray = Array.from(message.labels);
                    }
                    
                    if (labelsArray.length > 0) {
                        const labelsHtml = labelsArray.map(label =>
                            `<div class="card-label" style="background-color: ${label.color};" title="${label.name}">${label.name}</div>`
                        ).join('');
                        labelsContainer.html(labelsHtml);
                    } else {
                        // If labels array is empty, ensure container is empty
                        labelsContainer.empty();
                    }
                } else {
                    // If message.labels is null/undefined, ensure container is empty
                    labelsContainer.empty();
                }
            }
            
            // Update assignees
            if (message.assignees !== undefined) {
                const assigneesContainer = cardElement.find('.card-assignees');
                assigneesContainer.empty();
                
                if (message.assignees && message.assignees.length > 0) {
                    const assigneesHtml = message.assignees.map(assignee =>
                        `<div class="member-avatar" data-username="${assignee.username}" title="${assignee.username}">
                            ${assignee.username.substring(0, 1).toUpperCase()}
                        </div>`
                    ).join("");
                    assigneesContainer.html(assigneesHtml);
                } else {
                    // If assignees array is empty, ensure container is empty
                    assigneesContainer.empty();
                }
            }
        }
    }

    handleCardMove(message) {
        if (typeof $ === 'undefined') {
            return;
        }
        
        if (message.cardId && message.fromListId && message.toListId !== undefined) {
            const cardElement = $(`.card[data-card-id="${message.cardId}"]`);
            
            if (cardElement.length > 0) {
                
                // Remove card from current list
                cardElement.detach();
                
                // Find target list
                const targetList = $(`[data-list-id="${message.toListId}"]`);
                if (targetList.length > 0) {
                    const cardContainer = targetList.find('.card-container');
                    
                    // Insert card at new position
                    if (message.newPosition !== undefined && message.newPosition >= 0) {
                        const cards = cardContainer.children('.card');
                        if (message.newPosition >= cards.length) {
                            // Add to end
                            cardContainer.append(cardElement);
                        } else {
                            // Insert at specific position
                            cards.eq(message.newPosition).before(cardElement);
                        }
                    } else {
                        // Add to end if position not specified
                        cardContainer.append(cardElement);
                    }
                    
                } else {
                    // If target list not found, put card back in original position
                    const originalList = $(`[data-list-id="${message.fromListId}"]`);
                    if (originalList.length > 0) {
                        originalList.find('.card-container').append(cardElement);
                    }
                }
            }
        }
    }

    handleCardCreate(message) {
        if (typeof $ === 'undefined') {
            return;
        }
        
        if (message.cardId && message.title && message.listId) {
            const card = {
                id: message.cardId,
                title: message.title,
                listId: message.listId,
                position: message.position || 0
            };
            
            const cardHtml = this.createCardHtml(card);
            
            // Find the target list and add the card
            const targetList = $(`[data-list-id="${message.listId}"]`);
            
            if (targetList.length > 0) {
                targetList.find('.card-container').append(cardHtml);
            }
        }
    }

    handleCardDelete(message) {
        if (typeof $ === 'undefined') {
            return;
        }
        
        if (message.cardId) {
            const cardElement = $(`.card[data-card-id="${message.cardId}"]`);
            
            if (cardElement.length > 0) {
                // Add fade out animation before removing
                cardElement.fadeOut(300, function() {
                    $(this).remove();
                });
                
                // Close modal if it's open for this card
                const modal = $('#card-modal');
                if (modal.length > 0 && modal.data('card-id') == message.cardId) {
                    modal.hide();
                }
            }
        }
    }

    handleListUpdate(message) {
        if (typeof $ === 'undefined') {
            return;
        }
        
        if (message.listId && message.title) {
            const listElement = $(`.card-list[data-list-id="${message.listId}"]`);
            
            if (listElement.length > 0) {
                const titleElement = listElement.find('.list-title');
                if (titleElement.length > 0) {
                    titleElement.text(message.title);
                }
            }
        }
    }

    handleListMove(message) {
        if (typeof $ === 'undefined') {
            return;
        }
        
        if (message.listOrder && Array.isArray(message.listOrder)) {
            // Get the board container
            const boardContainer = $('#board-container');
            if (boardContainer.length === 0) {
                return;
            }
            
            // Get the add-list-wrapper element
            const addListWrapper = boardContainer.find('.add-list-wrapper');
            if (addListWrapper.length === 0) {
                return;
            }
            
            // Create a document fragment to hold all lists in the correct order
            const fragment = document.createDocumentFragment();
            
            // Collect all lists in the correct order
            message.listOrder.forEach((listId, index) => {
                const listElement = $(`.card-list[data-list-id="${listId}"]`);
                if (listElement.length > 0) {
                    // Detach the element and add to fragment
                    const detachedElement = listElement.detach()[0];
                    fragment.appendChild(detachedElement);
                }
            });
            
            // Insert all lists before the add-list-wrapper
            addListWrapper.before(fragment);
            
        }
    }

    handleListDelete(message) {
        if (typeof $ !== 'undefined') {
            $(`[data-list-id="${message.listId}"]`).remove();
        }
    }

    handleMemberJoin(message) {
        // Implementation for member join
    }

    handleMemberLeave(message) {
        // Implementation for member leave
    }

    handleAttachmentUpdate(message) {
        // Implementation for attachment updates
    }

    disconnect() {
        if (this.stompClient && this.isConnected) {
            try {
                this.stompClient.send('/app/board/' + this.boardId + '/leave', {}, this.username);
            } catch (e) {
            }
            this.stompClient.disconnect();
            this.isConnected = false;
            this.hasJoined = false;
        }
    }
}

// Global WebSocket manager instance
window.webSocketManager = new WebSocketManager();