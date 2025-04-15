    function uploadConfig() {
        const fileInput = document.getElementById('configFile');
        const file = fileInput.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const configContent = e.target.result;
                fetch('/upload', {
                    method: 'POST',
                    body: configContent,
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(response => {
                    if (response.redirected) {
                        window.location.href = response.url;
                        // Force iframe refresh by appending a timestamp
                        const iframe = document.getElementById('graphFrame');
                        iframe.src = '/app/graph.html?t=' + new Date().getTime();
                    }
                }).catch(error => {
                    console.error('Upload failed:', error);
                });
            };
            reader.readAsText(file);
        } else {
            alert('Please select a configuration file.');
        }
    }