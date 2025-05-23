/**
 * instructions.js
 * 
 * This module handles the display and toggling of instructions and available agents sections.
 * It provides functionality to show/hide these sections when the corresponding toggle buttons are clicked.
 * The module initializes event listeners on page load to respond to user interactions.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Get the elements
    const instructionsToggle = document.getElementById('instructions-toggle');
    const instructionsContent = document.getElementById('instructions-content');
    const instructionsContainer = document.getElementById('instructions-container');
    const toggleButton = instructionsToggle.querySelector('button');
    const secondToggleButton = document.getElementById('instructions-toggle-button');

    // Get the elements for available agents section
    const availableAgentsToggleButton = document.getElementById('available-agents-toggle-button');
    const availableAgentsContainer = document.getElementById('available-agents-container');

    /**
     * Toggles the visibility of the instructions section.
     * 
     * When called, this function will switch the instructions section between
     * visible and hidden states, and update the text on all related toggle buttons.
     */
    const toggleInstructions = () => {
        if (instructionsContainer.style.display === 'none' ||
            instructionsContainer.style.display === '') {
            instructionsContainer.style.display = 'block';
            instructionsContainer.classList.add('show');
            toggleButton.textContent = 'Hide Instructions';
            if (secondToggleButton) {
                secondToggleButton.textContent = 'Hide Instructions';
            }
        } else {
            instructionsContainer.style.display = 'none';
            instructionsContainer.classList.remove('show');
            toggleButton.textContent = 'Show Instructions';
            if (secondToggleButton) {
                secondToggleButton.textContent = 'Show Instructions';
            }
        }
    };

    /**
     * Toggles the visibility of the available agents section.
     * 
     * When called, this function will switch the available agents section between
     * visible and hidden states, and update the text on the toggle button.
     */
    const toggleAvailableAgents = () => {
        if (availableAgentsContainer.style.display === 'none' ||
            availableAgentsContainer.style.display === '') {
            availableAgentsContainer.style.display = 'block';
            availableAgentsContainer.classList.add('show');
            availableAgentsToggleButton.textContent = 'Hide Available Agents';
        } else {
            availableAgentsContainer.style.display = 'none';
            availableAgentsContainer.classList.remove('show');
            availableAgentsToggleButton.textContent = 'Show Available Agents';
        }
    };

    // Add click event to the original toggle button
    if (toggleButton) {
        toggleButton.addEventListener('click', (event) => {
            // Prevent event bubbling
            event.stopPropagation();
            toggleInstructions();
        });
    }

    // Add click event to the second toggle button (if it exists)
    if (secondToggleButton) {
        secondToggleButton.addEventListener('click', (event) => {
            // Prevent event bubbling
            event.stopPropagation();
            toggleInstructions();
        });
    }

    // Add click event to the available agents toggle button
    if (availableAgentsToggleButton) {
        availableAgentsToggleButton.addEventListener('click', (event) => {
            // Prevent event bubbling
            event.stopPropagation();
            toggleAvailableAgents();
        });
    }

    // For debugging
    console.log('Instructions toggle handlers initialized');
}); 