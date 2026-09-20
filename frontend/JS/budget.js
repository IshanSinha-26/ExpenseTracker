// =========================
// AUTHENTICATION CHECK
// =========================

const storedUserId =
    localStorage.getItem("userId");

if (!storedUserId) {

    window.location.href =
        "login.html";
}

const BUDGET_API_URL =
    "http://localhost:8080/api/budget";

const USER_ID =
    Number(localStorage.getItem("userId"));


// =========================
// LOAD CURRENT BUDGET
// =========================

async function loadBudget() {

    try {

        const response =
            await fetch(
                `${BUDGET_API_URL}?userId=${USER_ID}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load budget"
            );
        }


        const data =
            await response.json();


        console.log(
            "Current budget:",
            data
        );


        document.getElementById(
            "currentBudget"
        ).textContent =
            Number(data.amount || 0)
                .toLocaleString(
                    "en-IN",
                    {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                    }
                );


        // Put current budget
        // inside input

        if (data.amount > 0) {

            document.getElementById(
                "budgetAmount"
            ).value =
                data.amount;
        }


    } catch (error) {

        console.error(
            "Budget loading error:",
            error
        );

    }
}


// =========================
// SAVE BUDGET
// =========================

async function saveBudget(
    event
) {

    event.preventDefault();


    const amount =
        document.getElementById(
            "budgetAmount"
        ).value;


    if (!amount ||
        Number(amount) <= 0) {

        showMessage(
            "Please enter a valid budget.",
            true
        );

        return;
    }


    try {

        const response =
            await fetch(
                BUDGET_API_URL,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        userId: USER_ID,

                        amount:
                            Number(amount)

                    })
                }
            );


        const data =
            await response.json();


        console.log(
            "Save budget response:",
            data
        );


        if (!response.ok) {

            throw new Error(
                data.error ||
                "Failed to save budget"
            );
        }


        showMessage(
            "Budget saved successfully!",
            false
        );


        // Reload current budget

        loadBudget();


    } catch (error) {

        console.error(
            "Budget save error:",
            error
        );


        showMessage(
            error.message,
            true
        );
    }
}


// =========================
// MESSAGE
// =========================

function showMessage(
    message,
    isError
) {

    const messageElement =
        document.getElementById(
            "message"
        );


    messageElement.textContent =
        message;


    if (isError) {

        messageElement.style.color =
            "red";

    } else {

        messageElement.style.color =
            "green";
    }
}


// =========================
// INITIALIZE
// =========================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadBudget();


        document
            .getElementById("budgetForm")
            .addEventListener(
                "submit",
                saveBudget
            );

    }
);