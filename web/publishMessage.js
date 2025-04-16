function publishMessage(event) {
    event.preventDefault(); // Prevent the default form submission

    const topicInput = document.getElementById('topic');
    const messageInput = document.getElementById('message');
    const topic = topicInput.value;
    const message = messageInput.value;

    if (!topic || !message) {
        alert('Please enter both topic and message.');
        return;
    }

    // Send the message using fetch instead of form submission
    fetch(`/publish?topic=${encodeURIComponent(topic)}&message=${encodeURIComponent(message)}`, {
        method: 'GET'
    })
    .then(response => response.text())
    .then(htmlContent => {
        const iframe = document.getElementById('graphFrame');
        const blob = new Blob([htmlContent], { type: 'text/html' });
        const url = URL.createObjectURL(blob);
        iframe.src = url;
    })
    .catch(error => {
        console.error('Error publishing message:', error);
        alert('Failed to publish message. See console for details.');
    });
}

document.addEventListener('DOMContentLoaded', () => {
    // Add submit event listener to the form
    const form = document.querySelector('form');
    form.addEventListener('submit', (event) => publishMessage(event));
});