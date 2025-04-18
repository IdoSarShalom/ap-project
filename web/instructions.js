/**
 * Instructions toggle functionality
 * This file handles the show/hide toggle for the instructions section
 */

document.addEventListener('DOMContentLoaded', () => {
    // Get the elements
    const instructionsToggle = document.getElementById('instructions-toggle');
    const instructionsContent = document.getElementById('instructions-content');
    const instructionsContainer = document.getElementById('instructions-container');
    const toggleButton = instructionsToggle.querySelector('button');
    const secondToggleButton = document.getElementById('instructions-toggle-button');

    // Function to toggle instruction visibility
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

    // Add click event to the original toggle button
    toggleButton.addEventListener('click', (event) => {
        // Prevent event bubbling
        event.stopPropagation();
        toggleInstructions();
    });

    // Add click event to the second toggle button (if it exists)
    if (secondToggleButton) {
        secondToggleButton.addEventListener('click', (event) => {
            // Prevent event bubbling
            event.stopPropagation();
            toggleInstructions();
        });
    }

    // For debugging
    console.log('Instructions toggle handlers initialized');
}); 