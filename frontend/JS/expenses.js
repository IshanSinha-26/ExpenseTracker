// =========================
// AUTHENTICATION CHECK
// =========================

const storedUserId =
    localStorage.getItem("userId");

if (!storedUserId) {

    window.location.href =
        "login.html";
}

fetch("http://localhost:8080/api/test")
    .then(response => response.json())
    .then(data => {
        console.log(data.message);
    })
    .catch(error => {
        console.error("Backend connection failed:", error);
    });

let expenses = [];

const USER_ID =
    Number(localStorage.getItem("userId"));

const API_URL =
    "http://localhost:8080/api/expenses";

const CATEGORY_API_URL =
    "http://localhost:8080/api/categories";

const expenseTable =
    document.getElementById("expenseTable");

const expenseCount =
    document.getElementById("expenseCount");

const searchExpense =
    document.getElementById("searchExpense");

const categoryFilter =
    document.getElementById("categoryFilter");

const dateFilter =
    document.getElementById("dateFilter");

const expenseModal =
    document.getElementById("expenseModal");

const openAddExpense =
    document.getElementById("openAddExpense");

const closeModal =
    document.getElementById("closeModal");

const expenseForm =
    document.getElementById("expenseForm");

const modalTitle =
    document.getElementById("modalTitle");


/* =========================
   DISPLAY EXPENSES
========================= */

function displayExpenses(list) {

    expenseTable.innerHTML = "";


    // Header

    const header =
        document.createElement("div");

    header.className =
        "expense-table-row header";

    header.innerHTML = `
        <span>Category</span>
        <span>Description</span>
        <span>Amount</span>
        <span>Date</span>
        <span>Actions</span>
    `;

    expenseTable.appendChild(header);


    if (list.length === 0) {

        const empty =
            document.createElement("div");

        empty.className =
            "empty-state";

        empty.textContent =
            "No expenses found.";

        expenseTable.appendChild(empty);

        expenseCount.textContent =
            "0 expenses";

        return;
    }


    list.forEach(function (expense) {

        const row =
            document.createElement("div");

        row.className =
            "expense-table-row";


        row.innerHTML = `

            <span>
                ${expense.category}
            </span>

            <span>
                ${expense.description}
            </span>

            <span>
                ₹${expense.amount.toFixed(2)}
            </span>

            <span>
                ${formatDate(expense.date)}
            </span>

            <div class="action-buttons">

                <button
                    class="edit-btn"
                    onclick="editExpense(${expense.id})">

                    Edit

                </button>

                <button
                    class="delete-btn"
                    onclick="deleteExpense(${expense.id})">

                    Delete

                </button>

            </div>
        `;


        expenseTable.appendChild(row);

    });


    expenseCount.textContent =
        `${list.length} expense${list.length !== 1 ? "s" : ""}`;
}


/* =========================
   FORMAT DATE
========================= */

function formatDate(date) {

    const dateObject =
        new Date(date);

    return dateObject.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );
}


/* =========================
   FILTER EXPENSES
========================= */

function filterExpenses() {

    const search =
        searchExpense.value
            .toLowerCase()
            .trim();

    const category =
        categoryFilter.value;

    const date =
        dateFilter.value;


    const filtered =
        expenses.filter(function (expense) {

            const matchesSearch =
                expense.description
                    .toLowerCase()
                    .includes(search)
                ||
                expense.category
                    .toLowerCase()
                    .includes(search);


            const matchesCategory =
                category === "all"
                ||
                expense.category === category;


            const matchesDate =
                date === ""
                ||
                expense.date === date;


            return (
                matchesSearch
                &&
                matchesCategory
                &&
                matchesDate
            );

        });


    displayExpenses(filtered);
}


/* =========================
   SEARCH
========================= */

searchExpense.addEventListener(
    "input",
    filterExpenses
);


/* =========================
   CATEGORY FILTER
========================= */

categoryFilter.addEventListener(
    "change",
    filterExpenses
);


/* =========================
   DATE FILTER
========================= */

dateFilter.addEventListener(
    "change",
    filterExpenses
);


/* =========================
   OPEN ADD MODAL
========================= */

openAddExpense.addEventListener(
    "click",
    function () {

        modalTitle.textContent =
            "Add Expense";

        expenseForm.reset();

        document.getElementById(
            "expenseId"
        ).value = "";

        expenseModal.classList.add(
            "show"
        );

    }
);


/* =========================
   CLOSE MODAL
========================= */

closeModal.addEventListener(
    "click",
    function () {

        expenseModal.classList.remove(
            "show"
        );

    }
);


/* =========================
   ADD / UPDATE EXPENSE
========================= */

expenseForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const amount =
            parseFloat(
                document.getElementById(
                    "expenseAmount"
                ).value
            );


        const categoryId =
            parseInt(
                document.getElementById(
                    "expenseCategory"
                ).value
            );


        const description =
            document.getElementById(
                "expenseDescription"
            ).value.trim();


        const date =
            document.getElementById(
                "expenseDate"
            ).value;


        const expenseId =
            document.getElementById(
                "expenseId"
            ).value;


        /* =========================
           VALIDATION
        ========================= */

        if (amount <= 0) {

            alert(
                "Amount must be greater than 0."
            );

            return;
        }


        if (!categoryId) {

            alert(
                "Please select a category."
            );

            return;
        }


        if (!description) {

            alert(
                "Please enter a description."
            );

            return;
        }


        if (!date) {

            alert(
                "Please select a date."
            );

            return;
        }


        /* =========================
           EXPENSE DATA
        ========================= */

        const expenseData = {

            userId: USER_ID,

            categoryId: categoryId,

            amount: amount,

            description: description,

            date: date
        };


        /* =========================
           EDIT MODE
        ========================= */

        if (expenseId) {

            expenseData.id =
                parseInt(expenseId);


            console.log(
                "Updating expense:",
                expenseData
            );


            try {

                const response =
                    await fetch(
                        API_URL,
                        {
                            method: "PUT",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(
                                    expenseData
                                )
                        }
                    );


                const result =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        result.error ||
                        "Failed to update expense"
                    );
                }


                alert(
                    "Expense updated successfully!"
                );


                expenseModal.classList.remove(
                    "show"
                );


                expenseForm.reset();


                document.getElementById(
                    "expenseId"
                ).value = "";


                await loadExpenses();


            } catch (error) {

                console.error(
                    "Error updating expense:",
                    error
                );


                alert(
                    "Failed to update expense."
                );
            }


            return;
        }


        /* =========================
           ADD MODE
        ========================= */

        console.log(
            "Adding expense:",
            expenseData
        );


        try {

            const response =
                await fetch(
                    API_URL,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                expenseData
                            )
                    }
                );


            const result =
                await response.json();


            if (!response.ok) {

                throw new Error(
                    result.error ||
                    "Failed to add expense"
                );
            }


            alert(
                "Expense added successfully!"
            );


            expenseModal.classList.remove(
                "show"
            );


            expenseForm.reset();


            document.getElementById(
                "expenseId"
            ).value = "";


            await loadExpenses();


        } catch (error) {

            console.error(
                "Error adding expense:",
                error
            );


            alert(
                "Failed to add expense."
            );
        }

    }
);


/* =========================
   EDIT EXPENSE
========================= */

function editExpense(id) {

    const expense =
        expenses.find(
            function (expense) {

                return expense.id === id;

            }
        );


    if (!expense) {
        return;
    }


    modalTitle.textContent =
        "Edit Expense";


    document.getElementById(
        "expenseId"
    ).value = expense.id;


    document.getElementById(
        "expenseAmount"
    ).value = expense.amount;


    document.getElementById(
        "expenseCategory"
    ).value = expense.categoryId;


    document.getElementById(
        "expenseDescription"
    ).value = expense.description;


    document.getElementById(
        "expenseDate"
    ).value = expense.date;


    expenseModal.classList.add(
        "show"
    );
}


/* =========================
   DELETE EXPENSE
========================= */

async function deleteExpense(id) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this expense?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_URL}?id=${id}&userId=${USER_ID}`,
                {
                    method: "DELETE"
                }
            );


        const result =
            await response.json();


        if (!response.ok) {

            throw new Error(
                result.error ||
                "Failed to delete expense"
            );
        }


        console.log(
            "Delete response:",
            result
        );


        alert(
            "Expense deleted successfully!"
        );


        // Reload from database
        // instead of only removing from
        // the browser array.

        await loadExpenses();


    } catch (error) {

        console.error(
            "Error deleting expense:",
            error
        );


        alert(
            "Failed to delete expense."
        );
    }
}


/* =========================
   INITIAL DISPLAY
========================= */
async function loadExpenses() {

    try {

        const response =
            await fetch(
                `${API_URL}?userId=${USER_ID}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load expenses"
            );
        }


        expenses =
            await response.json();


        displayExpenses(expenses);


    } catch (error) {

        console.error(
            "Error loading expenses:",
            error
        );

        expenseTable.innerHTML = `
            <div class="empty-state">
                Failed to load expenses.
            </div>
        `;
    }
}

async function loadCategories() {

    const categorySelect =
        document.getElementById("expenseCategory");

    if (!categorySelect) {
        console.error("Category dropdown not found!");
        return;
    }

    try {

        const response = await fetch(
            `${CATEGORY_API_URL}?userId=${USER_ID}`
        );;

        if (!response.ok) {
            throw new Error(
                "Failed to load categories"
            );
        }

        const categories =
            await response.json();

        console.log(
            "Categories received:",
            categories
        );

        // Clear existing options

        categorySelect.innerHTML = "";

        // Default option

        const defaultOption =
            document.createElement("option");

        defaultOption.value = "";

        defaultOption.textContent =
            "Select Category";

        defaultOption.disabled = true;

        defaultOption.selected = true;

        categorySelect.appendChild(
            defaultOption
        );

        // Add categories

        categories.forEach(category => {

            const option =
                document.createElement("option");

            option.value =
                category.id;

            option.textContent =
                category.name;

            categorySelect.appendChild(
                option
            );
        });

    } catch (error) {

        console.error(
            "Error loading categories:",
            error
        );
    }
}

loadCategories();
loadExpenses();