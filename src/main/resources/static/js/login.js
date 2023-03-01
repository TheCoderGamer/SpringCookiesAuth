
function addOtherUserOption() {
    // check if otherUser select exists
    const select = document.querySelector('select[id="lastUser"]');
    if (select === null) { return; }

    const otherOption = document.createElement("option");


    let otherValue = ""
    for (let i = 0; i < select.options.length; i++) {
        if (select.options[i].value === "other") {
            continue;
        }
        otherValue += select.options[i].value + ",";
    }

    otherOption.value = otherValue;
    otherOption.text = "-- Select other user --";
    select.add(otherOption);
    select.addEventListener("change", function() {
        if (this.value === otherValue) {
            otherUser();
        } else {
            // remove input
            const input = document.querySelector('input[name="username"]');
            if (input !== null) {
                input.remove();
            }
            // set select name
            select.name = "username";
        }
    });
}

function otherUser() {
    const select = document.querySelector('select[id="lastUser"]');
    const input = document.createElement("input");
    input.type = "text";
    input.name = "username";
    input.placeholder = "Enter username";
    select.parentNode.insertBefore(input, select.nextSibling);
    select.name = "lastUser";
}