document.addEventListener('DOMContentLoaded', () => {
    // Get DOM elements
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

    // Template buttons
    const simpleTemplateBtn = document.getElementById('simple-template');
    const calculatorTemplateBtn = document.getElementById('calculator-template');
    const pipelineTemplateBtn = document.getElementById('pipeline-template');
    const advancedPipelineTemplateBtn = document.getElementById('advanced-pipeline-template');
    const dataProcessorTemplateBtn = document.getElementById('data-processor-template');
    const mathOperationsTemplateBtn = document.getElementById('math-operations-template');

    console.log("Initial DOM elements loaded:", {
        configTextarea: configTextarea ? "Found" : "Not found",
        simpleTemplateBtn: simpleTemplateBtn ? "Found" : "Not found",
        calculatorTemplateBtn: calculatorTemplateBtn ? "Found" : "Not found",
        pipelineTemplateBtn: pipelineTemplateBtn ? "Found" : "Not found",
        advancedPipelineTemplateBtn: advancedPipelineTemplateBtn ? "Found" : "Not found",
        dataProcessorTemplateBtn: dataProcessorTemplateBtn ? "Found" : "Not found",
        mathOperationsTemplateBtn: mathOperationsTemplateBtn ? "Found" : "Not found"
    });

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

    // Define simple templates directly here as fallback
    const LOCAL_TEMPLATES = {
        simpleCounter: `IncAgent
A
B`,
        calculator: `IncAgent
A
D
PlusAgent
D,E
F
MultAgent
F,G
H`,
        pipeline: `IncAgent
A
B
DoubleAgent
B
C
PlusAgent
C,D
E
IncAgent
D
F`,
        advancedPipeline: `IncAgent
A
B
DecAgent
B
C
PlusAgent
C,D
E
MultAgent
E,F
G
MinAgent
G,H
I
MaxAgent
I,J
K
IncAgent
A
D
DecAgent
A
F
DoubleAgent
A
H
IncAgent
A
J`,
        dataProcessor: `AbsAgent
A
B
NegAgent
B
C
DoubleAgent
C
D
MinusAgent
A,D
E`,
        mathOperations: `IncAgent
A
B
DecAgent
A
C
NegAgent
A
D
AbsAgent
D
E
DoubleAgent
A
F
PlusAgent
B,C
G
MinusAgent
B,D
H
MultAgent
E,F
I
MaxAgent
G,H
J
MinAgent
I,J
K
AvgAgent
K,J
L`
    };

    /**
     * Loads a template into the text area
     * @param {string} templateName - The name of the template to load
     */
    const loadTemplate = (templateName) => {
        console.log(`Attempting to load template: ${templateName}`);

        // Always switch to text input mode first
        toggleInputMode('text');

        // Make sure we have the textarea
        if (!configTextarea) {
            console.error("ERROR: Could not find textarea element with id 'configText'");
            displayConfigError("System error: Could not find template text area");
            return;
        }

        // Try to get template content
        let templateContent = null;

        // First try the global TEMPLATES object
        if (window.TEMPLATES && window.TEMPLATES[templateName]) {
            templateContent = window.TEMPLATES[templateName];
            console.log(`Using template from window.TEMPLATES: ${templateName}`);
        }
        // Fall back to local templates if needed
        else if (LOCAL_TEMPLATES[templateName]) {
            templateContent = LOCAL_TEMPLATES[templateName];
            console.log(`Using fallback template: ${templateName}`);
        }
        // No template found
        else {
            console.error(`Template '${templateName}' not found`);
            displayConfigError(`Error: Template '${templateName}' not found`);
            return;
        }

        // Update the textarea with template content
        try {
            console.log(`Setting textarea value to: ${templateContent.substring(0, 20)}...`);
            configTextarea.value = templateContent;
            clearConfigError();
            console.log(`Template '${templateName}' loaded successfully`);
        } catch (error) {
            console.error("Error setting textarea value:", error);
            displayConfigError(`Error loading template: ${error.message}`);
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

    // Set up event listeners for the template buttons
    const setupTemplateButtons = () => {
        if (simpleTemplateBtn) {
            simpleTemplateBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log("Simple template button clicked");
                loadTemplate('simpleCounter');
            });
        } else {
            console.warn("Simple template button not found");
        }

        if (calculatorTemplateBtn) {
            calculatorTemplateBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log("Calculator template button clicked");
                loadTemplate('calculator');
            });
        } else {
            console.warn("Calculator template button not found");
        }

        if (pipelineTemplateBtn) {
            pipelineTemplateBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log("Pipeline template button clicked");
                loadTemplate('pipeline');
            });
        } else {
            console.warn("Pipeline template button not found");
        }

        if (advancedPipelineTemplateBtn) {
            advancedPipelineTemplateBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log("Advanced Pipeline template button clicked");
                loadTemplate('advancedPipeline');
            });
        } else {
            console.warn("Advanced Pipeline template button not found");
        }

        if (dataProcessorTemplateBtn) {
            dataProcessorTemplateBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log("Data Processor template button clicked");
                loadTemplate('dataProcessor');
            });
        } else {
            console.warn("Data Processor template button not found");
        }

        if (mathOperationsTemplateBtn) {
            mathOperationsTemplateBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log("Math Operations template button clicked");
                loadTemplate('mathOperations');
            });
        } else {
            console.warn("Math Operations template button not found");
        }
    };

    // Set up the main event listeners
    const setupEventListeners = () => {
        console.log("Setting up event listeners...");

        // Deploy Button
        if (deployButton) {
            deployButton.addEventListener('click', handleDeployClick);
        } else {
            console.warn("Deploy button not found");
        }

        // Toggle Buttons
        if (fileInputToggle && textInputToggle) {
            fileInputToggle.addEventListener('click', () => toggleInputMode('file'));
            textInputToggle.addEventListener('click', () => toggleInputMode('text'));
        } else {
            console.warn("Toggle buttons not found");
        }

        // Set up template buttons
        setupTemplateButtons();
    };

    // Initialize the application
    console.log("DOM fully loaded. Initializing application...");
    clearConfigError();
    toggleInputMode('file'); // Start with file input mode active
    setupEventListeners();
});