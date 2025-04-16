document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('configFile');
    const deployButton = document.getElementById('deploy-btn');
    const errorArea = document.getElementById('config-error-area');
    const graphFrame = document.getElementById('graphFrame');

    /**
     * Displays an error message in the config error area.
     * @param {string} message - The error message to display.
     */
    const displayConfigError = (message) => {
        errorArea.textContent = message;
        errorArea.style.display = 'block'; // Make sure it's visible
    };

    /**
     * Clears the config error area.
     */
    const clearConfigError = () => {
        errorArea.textContent = '';
        errorArea.style.display = 'none'; // Hide it
    };

    /**
     * Updates the graph iframe with new HTML content.
     * @param {string} htmlContent - The HTML content for the iframe.
     */
    const updateGraphFrame = (htmlContent) => {
        // Use a Blob to handle the HTML content safely
        const blob = new Blob([htmlContent], { type: 'text/html' });
        const url = URL.createObjectURL(blob);

        // Set the iframe source. Revoke the old URL if it exists to prevent memory leaks.
        if (graphFrame.dataset.blobUrl) {
            URL.revokeObjectURL(graphFrame.dataset.blobUrl);
        }
        graphFrame.src = url;
        graphFrame.dataset.blobUrl = url; // Store the URL for future revocation
    };

    /**
     * Reads the selected configuration file and sends it to the server.
     * Handles success and error responses.
     */
    const handleDeployClick = async () => {
        clearConfigError(); // Clear previous errors on new attempt

        const file = fileInput.files[0];

        if (!file) {
            displayConfigError('Please select a configuration file.');
            return;
        }

        const reader = new FileReader();

        reader.onload = async (e) => {
            const configContent = e.target.result;

            try {
                const response = await fetch('/upload', {
                    method: 'POST',
                    body: configContent,
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                });

                const responseText = await response.text();

                if (response.ok) {
                    // Success (2xx status code)
                    updateGraphFrame(responseText);
                } else {
                    // Error (4xx or 5xx status code)
                    displayConfigError(`Error ${response.status}: ${responseText}`);
                    // Optionally reset the iframe on error:
                    // graphFrame.src = 'temp.html';
                    // if (graphFrame.dataset.blobUrl) {
                    //     URL.revokeObjectURL(graphFrame.dataset.blobUrl);
                    //     delete graphFrame.dataset.blobUrl;
                    // }
                }

            } catch (error) {
                // Network error or other issue with fetch
                console.error('Upload failed:', error);
                displayConfigError('Upload failed. Check console for details.');
            }
        };

        reader.onerror = (e) => {
            console.error('File reading error:', e);
            displayConfigError('Failed to read the selected file.');
        };

        reader.readAsText(file);
    };

    // --- Event Listeners --- 
    if (deployButton) {
        deployButton.addEventListener('click', handleDeployClick);
    }

    // Initial setup
    clearConfigError();
});