function toggleList(element) {
    // Toggle the visibility of list items
    const listItem = element.querySelector(".toggle-list-sublist")[0]
    if(listItem == null){
        console.error("Could not find sublist");
        return;
    }
    if (listItem.classList.contains('expand')) {
        listItem.classList.remove('expand');
    } else {
        listItem.classList.add('expand');
    }
}

window.addEventListener("load", () => {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-tooltip="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
})