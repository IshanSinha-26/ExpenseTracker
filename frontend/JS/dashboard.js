// =========================
// AUTHENTICATION CHECK
// =========================

const storedUserId =
    localStorage.getItem("userId");

if (!storedUserId) {

    window.location.href =
        "login.html";
}

const DASHBOARD_API_URL =
    "http://localhost:8080/api/dashboard";

const USER_ID =
    Number(localStorage.getItem("userId"));


// =========================
// LOAD DASHBOARD
// =========================

async function loadDashboard() {

    try {

        const response = await fetch(
            `${DASHBOARD_API_URL}?userId=${USER_ID}`
        );


        if (!response.ok) {

            throw new Error(
                "Failed to load dashboard data"
            );
        }


        const data =
            await response.json();


        console.log(
            "Dashboard data received:",
            data
        );

        displaySummary(data);

        displayCategorySpending(
            data.categories
        );

        displayRecentExpenses(
            data.recentExpenses
        );

        displayMonthlyChart(
            data.monthlyChart
        );

        displayBudgetAlert(
            data
        );

        loadBudget();

    }
    catch (error) {

        console.error(
            "Dashboard error:",
            error
        );

    }
}

// =========================
// LOAD BUDGET
// =========================

async function loadBudget() {

    try {

        const response =
            await fetch(
                `http://localhost:8080/api/budget?userId=${USER_ID}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load budget"
            );
        }


        const budget =
            await response.json();


        console.log(
            "Budget data received:",
            budget
        );


        displayBudget(
            budget.amount
        );


    } catch (error) {

        console.error(
            "Budget error:",
            error
        );

    }
}

// =========================
// DISPLAY BUDGET
// =========================

function displayBudget(
    budgetAmount
) {

    const budgetElement =
        document.getElementById(
            "monthlyBudget"
        );


    const remainingElement =
        document.getElementById(
            "remainingBudget"
        );


    if (budgetElement) {

        budgetElement.textContent =
            formatCurrency(
                budgetAmount
            );
    }


    if (remainingElement) {

        const monthlySpendingElement =
            document.getElementById(
                "monthlySpending"
            );


        const monthlySpending =
            getNumericValue(
                monthlySpendingElement
            );


        const remaining =
            Number(budgetAmount) -
            monthlySpending;


        remainingElement.textContent =
            formatCurrency(
                remaining
            );
    }
}

// =========================
// GET NUMERIC VALUE
// =========================

function getNumericValue(element) {

    if (!element) {

        return 0;
    }


    const value =
        element.textContent
            .replace(/[₹,]/g, "")
            .trim();


    return Number(value) || 0;
}

// =========================
// SUMMARY
// =========================

function displaySummary(data) {

    const totalSpending =
        document.getElementById(
            "totalSpending"
        );


    const monthlySpending =
        document.getElementById(
            "monthlySpending"
        );


    if (totalSpending) {

        totalSpending.textContent =
            formatCurrency(
                data.totalSpending
            );
    }


    if (monthlySpending) {

        monthlySpending.textContent =
            formatCurrency(
                data.monthlySpending
            );
    }
}


// =========================
// CATEGORY SPENDING
// =========================

function displayCategorySpending(
    categories
) {

    const container =
        document.getElementById(
            "categorySpending"
        );


    if (!container) {

        return;
    }


    container.innerHTML = "";


    if (
        !categories ||
        categories.length === 0
    ) {

        container.innerHTML =
            "<p>No category data available.</p>";

        return;
    }


    categories.forEach(
        function (category) {

            const item =
                document.createElement(
                    "div"
                );


            item.className =
                "category-item";


            item.innerHTML = `

                <span>
                    ${escapeHtml(category.name)}
                </span>

                <strong>
                    ${formatCurrency(category.amount)}
                </strong>

            `;


            container.appendChild(
                item
            );
        }
    );
}


// =========================
// RECENT EXPENSES
// =========================

function displayRecentExpenses(
    expenses
) {

    const container =
        document.getElementById(
            "recentExpenses"
        );


    if (!container) {

        return;
    }


    // Keep table header

    container.innerHTML = `

        <div class="expense-row header">

            <span>
                Category
            </span>

            <span>
                Description
            </span>

            <span>
                Amount
            </span>

            <span>
                Date
            </span>

        </div>

    `;


    if (
        !expenses ||
        expenses.length === 0
    ) {

        const row =
            document.createElement(
                "div"
            );


        row.className =
            "expense-row";


        row.innerHTML = `

            <span>
                No expenses
            </span>

            <span>
                -
            </span>

            <span>
                ₹0.00
            </span>

            <span>
                -
            </span>

        `;


        container.appendChild(
            row
        );

        return;
    }


    expenses.forEach(
        function (expense) {

            const row =
                document.createElement(
                    "div"
                );


            row.className =
                "expense-row";


            row.innerHTML = `

                <span>
                    ${escapeHtml(
                expense.category
            )}
                </span>

                <span>
                    ${escapeHtml(
                expense.description
            )}
                </span>

                <span>
                    ${formatCurrency(
                expense.amount
            )}
                </span>

                <span>
                    ${formatDate(
                expense.date
            )}
                </span>

            `;


            container.appendChild(
                row
            );
        }
    );
}


// =========================
// CURRENCY
// =========================

function formatCurrency(
    amount
) {

    return "₹" +
        Number(amount || 0)
            .toLocaleString(
                "en-IN",
                {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                }
            );
}


// =========================
// DATE
// =========================

function formatDate(
    dateString
) {

    if (!dateString) {

        return "-";
    }


    const date =
        new Date(
            dateString + "T00:00:00"
        );


    return date.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );
}


// =========================
// HTML SAFETY
// =========================

function escapeHtml(value) {

    if (value === null ||
        value === undefined) {

        return "";
    }


    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// =========================
// MONTHLY SPENDING CHART
// =========================

function displayMonthlyChart(monthlyData) {

    const chart =
        document.querySelector(".chart");

    if (!chart) {
        return;
    }

    chart.innerHTML = "";

    const monthNames = [
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    ];

    if (!monthlyData ||
        monthlyData.length === 0) {

        chart.innerHTML =
            "<p>No monthly spending data available.</p>";

        return;
    }


    // Find highest monthly amount

    const maxAmount =
        Math.max(
            ...monthlyData.map(
                item => Number(item.amount)
            )
        );


    // Create bars for all 12 months

    for (let i = 1; i <= 12; i++) {

        const monthData =
            monthlyData.find(
                item =>
                    Number(item.month) === i
            );


        const amount =
            monthData
                ? Number(monthData.amount)
                : 0;


        let height = 0;


        if (maxAmount > 0) {

            height =
                (amount / maxAmount) * 100;
        }


        const bar =
            document.createElement("div");

        bar.className = "bar";

        bar.style.height =
            `${height}%`;


        // Show amount when there is spending

        if (amount > 0) {

            bar.title =
                formatCurrency(amount);
        }


        const label =
            document.createElement("span");

        label.textContent =
            monthNames[i - 1];


        bar.appendChild(label);

        chart.appendChild(bar);
    }
}

// =========================
// BUDGET ALERT
// =========================

function displayBudgetAlert(data) {

    const alertElement =
        document.getElementById(
            "budgetAlert"
        );


    if (!alertElement) {

        return;
    }


    const status =
        data.status;


    const percentage =
        Number(
            data.percentage || 0
        );


    const remaining =
        Number(
            data.remaining || 0
        );


    const budget =
        Number(
            data.budget || 0
        );


    alertElement.className =
        "budget-alert";


    // =========================
    // NO BUDGET
    // =========================

    if (status === "NO_BUDGET") {

        alertElement.classList.add(
            "no-budget"
        );


        alertElement.innerHTML = `
            <strong>
                No monthly budget set.
            </strong>

            <span>
                Set a budget to start tracking
                your spending.
            </span>
        `;


        return;
    }


    // =========================
    // EXCEEDED
    // =========================

    if (status === "EXCEEDED") {

        const exceededAmount =
            Math.abs(remaining);


        alertElement.classList.add(
            "exceeded"
        );


        alertElement.innerHTML = `
            <strong>
                🚨 Budget exceeded!
            </strong>

            <span>
                You have exceeded your
                monthly budget by
                ${formatCurrency(
            exceededAmount
        )}.
            </span>
        `;


        return;
    }


    // =========================
    // WARNING
    // =========================

    if (status === "WARNING") {

        alertElement.classList.add(
            "warning"
        );


        alertElement.innerHTML = `
            <strong>
                ⚠️ Budget warning
            </strong>

            <span>
                You have used
                ${percentage.toFixed(2)}%
                of your monthly budget.
                Remaining:
                ${formatCurrency(
            remaining
        )}.
            </span>
        `;


        return;
    }


    // =========================
    // SAFE
    // =========================

    if (status === "SAFE") {

        alertElement.classList.add(
            "safe"
        );


        alertElement.innerHTML = `
            <strong>
                ✓ Budget is under control
            </strong>

            <span>
                You have used
                ${percentage.toFixed(2)}%
                of your monthly budget.
                Remaining:
                ${formatCurrency(
            remaining
        )}.
            </span>
        `;


        return;
    }
}


// =========================
// INITIAL LOAD
// =========================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadDashboard();

    }
);

// =========================
// LOGOUT
// =========================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        const logoutLink =
            document.getElementById(
                "logoutLink"
            );


        if (!logoutLink) {
            return;
        }


        logoutLink.addEventListener(
            "click",
            function (event) {

                event.preventDefault();


                // Remove logged-in user

                localStorage.removeItem(
                    "userId"
                );

                localStorage.removeItem(
                    "userEmail"
                );


                // Go to login page

                window.location.href =
                    "login.html";
            }
        );
    }
);