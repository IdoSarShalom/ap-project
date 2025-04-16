/**
 * Predefined configuration templates for the pub/sub system
 */

// Log when this script runs
console.log("templateConfigs.js is loading...");

// Define templates in the global scope
window.TEMPLATES = {
    // Simple counter template
    simpleCounter: "IncAgent\nA\nB",

    // Calculator template
    calculator: "IncAgent\nINPUT\nTMP1\nPlusAgent\nTMP1,TMP2\nOUTPUT\nIncAgent\nTMP2\nTMP2",

    // Pipeline template
    pipeline: "IncAgent\nINPUT\nSTEP1\nIncAgent\nSTEP1\nSTEP2A\nPlusAgent\nSTEP2A,STEP2B\nRESULT\nIncAgent\nSTEP1\nSTEP2B"
};

// Log available templates
console.log("Templates loaded:", Object.keys(window.TEMPLATES).join(", "));

// Make templates available even if window.TEMPLATES isn't accessible
try {
    if (typeof TEMPLATES === 'undefined') {
        // Create a global TEMPLATES variable as backup
        window.TEMPLATES = window.TEMPLATES || {};
        console.log("Created backup TEMPLATES object");
    }
} catch (e) {
    console.error("Error setting up templates:", e);
}