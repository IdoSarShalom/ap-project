document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('configFile');
    const configTextarea = document.getElementById('configText');
    const deployButton = document.getElementById('deploy-btn');
    const errorArea = document.getElementById('config-error-area');
    const graphFrame = document.getElementById('graphFrame');

    // Input toggle elements
    const fileInputToggle = document.getElementById('file-input-toggle');
    const textInputToggle = document.getElementById('text-input-toggle');
    const fileInputContainer = document.getElementById('file-input-container');
    const textInputContainer = document.getElementById('text-input-container');

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
     * Toggles between file input and text input modes
     * @param {string} mode - Either 'file' or 'text'
     */
    const toggleInputMode = (mode) => {
        if (mode === 'file') {
            fileInputContainer.style.display = 'block';
            textInputContainer.style.display = 'none';
            fileInputToggle.classList.add('active');
            textInputToggle.classList.remove('active');
        } else {
            fileInputContainer.style.display = 'none';
            textInputContainer.style.display = 'block';
            fileInputToggle.classList.remove('active');
            textInputToggle.classList.add('active');
        }
    };

    /**
     * Sends the configuration content to the server
     * @param {string} configContent - The configuration to send
     * @returns {Promise<void>}
     */
    const sendConfigToServer = async (configContent) => {
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
            }
        } catch (error) {
            // Network error or other issue with fetch
            console.error('Upload failed:', error);
            displayConfigError('Upload failed. Check console for details.');
        }
    };

    /**
     * Reads the selected configuration file and sends it to the server.
     * Handles success and error responses.
     */
    const handleDeployClick = async () => {
        clearConfigError(); // Clear previous errors on new attempt

        // Check which input mode is active
        const isFileMode = fileInputToggle.classList.contains('active');

        if (isFileMode) {
            // FILE INPUT MODE
            const file = fileInput.files[0];

            if (!file) {
                displayConfigError('Please select a configuration file.');
                return;
            }

            // Validate file extension
            if (!file.name.toLowerCase().endsWith('.conf')) {
                displayConfigError('Invalid file type. Please select a .conf file.');
                // Clear the file input for better UX
                fileInput.value = ''; // Reset the input
                return;
            }

            const reader = new FileReader();

            reader.onload = async (e) => {
                const configContent = e.target.result;
                await sendConfigToServer(configContent);
            };

            reader.onerror = (e) => {
                console.error('File reading error:', e);
                displayConfigError('Failed to read the selected file.');
            };

            reader.readAsText(file);
        } else {
            // TEXT INPUT MODE
            const configContent = configTextarea.value.trim();

            if (!configContent) {
                displayConfigError('Please enter configuration content.');
                return;
            }

            await sendConfigToServer(configContent);
        }
    };

    // --- Event Listeners ---
    if (deployButton) {
        deployButton.addEventListener('click', handleDeployClick);
    }

    // Add toggle functionality
    fileInputToggle.addEventListener('click', () => toggleInputMode('file'));
    textInputToggle.addEventListener('click', () => toggleInputMode('text'));

    // Initial setup
    clearConfigError();
    toggleInputMode('file'); // Start with file input mode active
});