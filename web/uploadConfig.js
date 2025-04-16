function uploadConfig() {
        const fileInput = document.getElementById('configFile');
        const file = fileInput.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = async (e) => {
                const configContent = e.target.result;
                const response = await fetch('/upload', {
                    method: 'POST',
                    body: configContent,
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                });

                const htmlContent = await response.text();
                const iframe = document.getElementById('graphFrame');
                const blob = new Blob([htmlContent], { type: 'text/html' });
                const url = URL.createObjectURL(blob);
                iframe.src = url;
            };
            reader.readAsText(file);
        } else {
            alert('Please select a configuration file.');
        }
    }

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('deploy-btn').addEventListener('click', () => uploadConfig());
})