/**
 * templateConfig.js
 * 
 * This module defines predefined configuration templates for the pub/sub system.
 * Each template represents a different application or pattern of the pub/sub system.
 * 
 * Configuration format:
 * - Each line in a template defines a component or connection in the system
 * - Agent definitions start with the agent type (e.g., "IncAgent")
 * - The line after the agent type specifies input topics (comma-separated if multiple)
 * - The next line specifies output topics
 * - Topic names are typically single uppercase letters (A-Z)
 */

// Log when this script runs
console.log("templateConfigs.js is loading...");

/**
 * Global templates object containing predefined configuration templates.
 * @namespace
 * @property {string} simpleCounter - A simple counter that increments values from topic A to B
 * @property {string} calculator - A basic calculator with increment, addition, and multiplication operations
 * @property {string} pipeline - A sequential pipeline of processing steps
 * @property {string} advancedPipeline - A complex pipeline with multiple processing branches
 * @property {string} dataProcessor - A data processor with absolute value, negation, doubling, and subtraction
 * @property {string} mathOperations - A comprehensive example using all available mathematical operations
 */
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

/**
 * Ensures that the TEMPLATES object is globally available
 * This is a fallback mechanism in case there are scope issues
 */
try {
    if (typeof TEMPLATES === 'undefined') {
        // Create a global TEMPLATES variable as backup
        window.TEMPLATES = window.TEMPLATES || {};
        console.log("Created backup TEMPLATES object");
    }
} catch (e) {
    console.error("Error setting up templates:", e);
}