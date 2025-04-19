/**
 * publishMessage.js
 * 
 * This module handles the publishing of messages to topics in the pub/sub system.
 * It provides form validation, error handling, and updates the graph visualization
 * when a message is successfully published.
 */
document.addEventListener('DOMContentLoaded', () => {
    const publishForm = document.querySelector('.left-panel form[action="/publish"]'); // More specific selector
    const topicInput = document.getElementById('topic');
    const messageInput = document.getElementById('message');
    const graphFrame = document.getElementById('graphFrame');
    const publishErrorArea = document.getElementById('publish-error-area'); // Get the new error area

    /**
     * Displays an error message in the publish error area.
     * @param {string} message - The error message to display.
     */
    const displayPublishError = (message) => {
        if (publishErrorArea) {
            publishErrorArea.textContent = message;
            publishErrorArea.style.display = 'block'; // Make sure it's visible
        }
    };

    /**
     * Clears the publish error area.
     */
    const clearPublishError = () => {
        if (publishErrorArea) {
            publishErrorArea.textContent = '';
            publishErrorArea.style.display = 'none'; // Hide it
        }
    };

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
        clearPublishError(); // Clear previous errors

        const topic = topicInput.value.trim();
        const message = messageInput.value.trim();

        if (!topic || !message) {
            displayPublishError('Please enter both topic and message.'); // Use new error display
            return;
        }

        // Validate Topic format (exactly one A-Z)
        const topicRegex = /^[A-Z]$/;
        if (!topicRegex.test(topic)) {
            displayPublishError('Invalid topic format. Topic must be exactly one uppercase letter (A-Z).');
            topicInput.focus(); // Focus the topic input for correction
            return;
        }

        // Validate if the message is a number (integer or double)
        if (isNaN(Number(message))) {
            displayPublishError('Invalid message format. Message must be a number (e.g., 5 or 3.14).'); // Use new error display
            messageInput.focus(); // Focus the message input for correction
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
                displayPublishError(`Error publishing message: ${response.status} ${responseText}`); // Use new error display
            }

        } catch (error) {
            console.error('Error publishing message:', error);
            displayPublishError('Failed to publish message. Check console for details.'); // Use new error display
        }
    };

    // --- Event Listeners ---
    if (publishForm) {
        publishForm.addEventListener('submit', handlePublishSubmit);
    }

    // Initial setup
    clearPublishError();
});