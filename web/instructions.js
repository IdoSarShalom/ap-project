/**
 * Instructions toggle functionality
 * This file handles the show/hide toggle for the instructions section
 */

document.addEventListener('DOMContentLoaded', () => {
    // Get the elements
    const instructionsToggle = document.getElementById('instructions-toggle');
    const instructionsContent = document.getElementById('instructions-content');
    const toggleButton = instructionsToggle.querySelector('button');

    // Make sure instructions are hidden by default
    instructionsContent.style.display = 'none';

    // Add click event to the toggle button
    toggleButton.addEventListener('click', (event) => {
        // Prevent event bubbling
        event.stopPropagation();

        // Toggle instructions visibility
        if (instructionsContent.style.display === 'none' ||
            instructionsContent.style.display === '') {
            instructionsContent.style.display = 'block';
            instructionsContent.classList.add('show');
            toggleButton.textContent = 'Hide Instructions';
        } else {
            instructionsContent.style.display = 'none';
            instructionsContent.classList.remove('show');
            toggleButton.textContent = 'Show Instructions';
        }
    });

    // For debugging
    console.log('Instructions toggle handler initialized');
}); 