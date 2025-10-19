async function clientSideInclude(id, url) {
    const element = document.getElementById(id);
    if (!element) {
        console.error(`Element with id '${id}' not found.`);
        return;
    }
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error: ${response.status}`);
        }
        const html = await response.text();
        element.innerHTML = html;
    } catch (err) {
        console.error(`Failed to fetch HTML: ${err.message}`);
    }
}
