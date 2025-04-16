document.addEventListener('DOMContentLoaded', () => {
    const publishForm = document.querySelector('.left-panel form[action="/publish"]'); // More specific selector
    const topicInput = document.getElementById('topic');
    const messageInput = document.getElementById('message');
    const graphFrame = document.getElementById('graphFrame');

    /**
     * Updates the graph iframe with new HTML content.
     * @param {string} htmlContent - The HTML content for the iframe.
     */
    const updateGraphFrame = (htmlContent) => {
        // Reuse the Blob logic for safe HTML handling
        const blob = new Blob([htmlContent], { type: 'text/html' });
        const url = URL.createObjectURL(blob);

        // Revoke the old URL if it exists
        if (graphFrame.dataset.blobUrl) {
            URL.revokeObjectURL(graphFrame.dataset.blobUrl);
        }

        graphFrame.src = url;
        graphFrame.dataset.blobUrl = url; // Store for future revocation
    };

    /**
     * Handles the submission of the publish message form.
     * @param {Event} event - The form submission event.
     */
    const handlePublishSubmit = async (event) => {
        event.preventDefault(); // Prevent default form submission

        const topic = topicInput.value.trim();
        const message = messageInput.value.trim();

        if (!topic || !message) {
            alert('Please enter both topic and message.');

            return;
        }

        const url = `/publish?topic=${encodeURIComponent(topic)}&message=${encodeURIComponent(message)}`;

        try {
            const response = await fetch(url, {
                method: 'GET' // Or POST if your backend expects it
            });

            const responseText = await response.text();

            if (response.ok) {
                // Assuming the response is the updated graph HTML
                updateGraphFrame(responseText);
                // Optionally clear the form fields on success
                // topicInput.value = '';
                // messageInput.value = '';
            } else {
                // Handle server-side errors if the publish endpoint can return them
                alert(`Error publishing message: ${response.status} ${responseText}`);
            }

        } catch (error) {
            console.error('Error publishing message:', error);
            alert('Failed to publish message. Check console for details.');
        }
    };

    // --- Event Listeners ---
    if (publishForm) {
        publishForm.addEventListener('submit', handlePublishSubmit);
    }
});