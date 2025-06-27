$(() => {

    const clientId = crypto.randomUUID();
    const privateClientId = crypto.randomUUID();
    const messages = $('#messages');
    const serverInfo = $('#serverInfo');
    const username = $('#username');
    const recipients = $('#recipients');
    const message = $('#message');
    const connectBtn = $('#connectBtn');
    const disconnectBtn = $('#disconnectBtn');
    const isVisibleBtn = $('#isVisibleBtn');
    const isBusyBtn = $('#isBusyBtn');
    const sendBtn = $('#sendBtn');
    const disabledProperty = 'disabled';

    let client = null;

    const updateView = (isConnected) => {
        username.prop(disabledProperty, isConnected);
        recipients.prop(disabledProperty, !isConnected);
        message.prop(disabledProperty, !isConnected);
        if (isConnected) {
            messages.text('');
        }
        connectBtn.prop(disabledProperty, isConnected);
        disconnectBtn.prop(disabledProperty, !isConnected);
        sendBtn.prop(disabledProperty, !isConnected);
        isVisibleBtn.prop(disabledProperty, !isConnected);
        isBusyBtn.prop(disabledProperty, !isConnected);
    };

    const connect = () => {
        const socket = new WebSocket("/chat")
        client = Stomp.over(socket);
        client.connect({username: username.val(), clientId, privateClientId}, onConnected);
    };

    const onConnected = () => {
        updateView(true);
        client.subscribe('/main', onMessage)
        client.subscribe('/user-list', onUserListUpdated)
        client.subscribe('/private-' + privateClientId, onMessage)
        //client.subscribe('/time', onTimeUpdated)
        client.send('/ws/readiness', {}, {});
    };

    const disconnect = () => {
        client.disconnect();
        updateView(false);
    };

    const send = () => {
        const text = message.val();
        if (text) {
            const chatMessage = {
                sender: username.val(),
                recipients: recipients.val(),
                text
            }
            console.log(chatMessage);
            client.send('/ws/chat', {}, JSON.stringify(chatMessage));
            message.text('');
        }
    };

    const onMessage = (socketMessage) => {
        const chatMessage = JSON.parse(socketMessage.body);
        const timestamp = new Date(chatMessage.timestamp).toLocaleTimeString();
        $(`<p>${timestamp} ${chatMessage.sender}: ${chatMessage.text}</p>`).appendTo(messages);
    };

    const onUserListUpdated = (socketMessage) => {
        const userList = JSON.parse(socketMessage.body);
        recipients.empty();
        userList
            .filter(user => user.id !== clientId)
            .forEach(user => {
                const status = user.busy ? ' busy' : '';
                $(`<option value="${user.id}">${user.name} (${user.id}) ${status}</option>`).appendTo(recipients)
            });
    };

    const onTimeUpdated = (socketMessage) => {
        const timestamp = JSON.parse(socketMessage.body);
        serverInfo.text('System time: ' + new Date(timestamp).toLocaleTimeString());
    };

    function changeVisibility() {
        let hidden = false;
        if($(this).is(':checked')) {
            hidden = true;
        }
        client.send('/ws/statuses', {}, JSON.stringify({hidden, busy: isBusyBtn.is(':checked')}));
    }

    function changeIsBusy() {
        let busy = false;
        if($(this).is(':checked')) {
            busy = true;
        }
        client.send('/ws/statuses', {}, JSON.stringify({busy, hidden: isVisibleBtn.is(':checked')}));
    }

    updateView(false);
    connectBtn.click(connect);
    disconnectBtn.click(disconnect);
    sendBtn.click(send);
    isVisibleBtn.change(changeVisibility);
    isBusyBtn.change(changeIsBusy);
});