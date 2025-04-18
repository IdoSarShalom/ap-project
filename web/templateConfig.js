/**
 * Predefined configuration templates for the pub/sub system
 */

// Log when this script runs
console.log("templateConfigs.js is loading...");

// Define templates in the global scope
window.TEMPLATES = {
    // Simple counter template
    simpleCounter: "IncAgent\nA\nB",

    // Calculator template with proper topic names
    calculator: "IncAgent\nA\nD\nPlusAgent\nD,E\nF\nMultAgent\nF,G\nH",

    // Pipeline template with proper topic names
    pipeline: "IncAgent\nA\nB\nDoubleAgent\nB\nC\nPlusAgent\nC,D\nE\nIncAgent\nD\nF",

    // Advanced Pipeline template with variety of agents
    advancedPipeline: "IncAgent\nA\nB\nDecAgent\nB\nC\nPlusAgent\nC,D\nE\nMultAgent\nE,F\nG\nMinAgent\nG,H\nI\nMaxAgent\nI,J\nK\nIncAgent\nA\nD\nDecAgent\nA\nF\nDoubleAgent\nA\nH\nIncAgent\nA\nJ",

    // Data Processor template with proper topic names
    dataProcessor: "AbsAgent\nA\nB\nNegAgent\nB\nC\nDoubleAgent\nC\nD\nMinusAgent\nA,D\nE",

    // Math Operations template with all operations
    mathOperations: "IncAgent\nA\nB\nDecAgent\nA\nC\nNegAgent\nA\nD\nAbsAgent\nD\nE\nDoubleAgent\nA\nF\nPlusAgent\nB,C\nG\nMinusAgent\nB,D\nH\nMultAgent\nE,F\nI\nMaxAgent\nG,H\nJ\nMinAgent\nI,J\nK\nAvgAgent\nK,J\nL"
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