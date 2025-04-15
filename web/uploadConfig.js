<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Pub/Sub System</title>
    <link rel="stylesheet" href="./style.css">
</head>
<body>
<div class="container">
    <div class="left-panel">
        <h2>Pub/Sub System</h2>
        <div>
            <h3>Config Upload</h3>
            <input type="file" id="configFile">
            <button id="deploy-btn">Deploy</button>
        </div>
        <div>
            <h3>Publish Message</h3>
            <form action="/publish" method="GET">
                <label for="topic">Topic name:</label>
                <input type="text" id="topic" name="topic" placeholder="e.g., B">
                <label for="message">Message:</label>
                <input type="text" id="message" name="message" placeholder="e.g., 5">
                <button type="submit">Send</button>
            </form>
        </div>
    </div>
    <div class="right-panel">
        <h2>Graph Visualization</h2>
        <iframe id="graphFrame" src="temp.html" width="100%" height="500px"></iframe>
    </div>
</div>
<script src="./uploadConfig.js"></script>
</body>
</html>
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
                console.log(htmlContent);

                const iframe = document.getElementById('graphFrame');

                // Create a blob with the HTML content
                const blob = new Blob([htmlContent], { type: 'text/html' });
                const url = URL.createObjectURL(blob);
                iframe.src = url;
            };
            reader.readAsText(file);
        } else {
            alert('Please select a configuration file.');
        }
    }


    document.addEventListener('DOMContentLoaded', ()=> {
        document.getElementById('deploy-btn').addEventListener('click', ()=> uploadConfig())
    })