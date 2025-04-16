function uploadConfig() {
        const fileInput = document.getElementById('configFile');
        const file = fileInput.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = async (e) => {
                const configContent = e.target.result;
                const response = await fetch('/upload', {
                    method: 'POST',
                    body: configContent,
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                });

                const htmlContent = await response.text();
                const iframe = document.getElementById('graphFrame');
                const blob = new Blob([htmlContent], { type: 'text/html' });
                const url = URL.createObjectURL(blob);
                iframe.src = url;
            };
            reader.readAsText(file);
        } else {
            alert('Please select a configuration file.');
        }
    }

function sendMessage(event) {
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
        console.error('Error sending message:', error);
        alert('Failed to send message. See console for details.');
    });
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('deploy-btn').addEventListener('click', () => uploadConfig());

    // Replace the old event handler with the new one
    const form = document.querySelector('form');
    form.addEventListener('submit', (event) => sendMessage(event));
})