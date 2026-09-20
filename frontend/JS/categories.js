// =========================
// AUTHENTICATION CHECK
// =========================

const storedUserId =
    localStorage.getItem("userId");

if (!storedUserId) {

    window.location.href =
        "login.html";
}

const CATEGORY_API_URL =
    "http://localhost:8080/api/categories";

const USER_ID = 1;


// =========================
// ELEMENTS
// =========================

const addCategoryButton =
    document.getElementById("addCategoryButton");

const categoryForm =
    document.getElementById("categoryForm");

const categoryName =
    document.getElementById("categoryName");

const saveCategoryButton =
    document.getElementById("saveCategoryButton");

const cancelCategoryButton =
    document.getElementById("cancelCategoryButton");

const categoryList =
    document.getElementById("categoryList");

const categoryMessage =
    document.getElementById("categoryMessage");


// =========================
// SHOW FORM
// =========================

addCategoryButton.addEventListener(
    "click",
    function () {

        categoryForm.classList.remove(
            "hidden"
        );

        categoryName.focus();

    }
);


// =========================
// CANCEL
// =========================

cancelCategoryButton.addEventListener(
    "click",
    function () {

        categoryForm.classList.add(
            "hidden"
        );

        categoryName.value = "";

        categoryMessage.textContent = "";

    }
);


// =========================
// LOAD CATEGORIES
// =========================

async function loadCategories() {

    try {

        const response = await fetch(
            `${CATEGORY_API_URL}?userId=${USER_ID}`
        );

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

        displayCategories(
            categories
        );

    } catch (error) {

        console.error(
            "Error loading categories:",
            error
        );

        categoryList.innerHTML =
            `<p>Failed to load categories.</p>`;
    }
}


// =========================
// DISPLAY CATEGORIES
// =========================

function displayCategories(
    categories
) {

    categoryList.innerHTML = "";

    if (categories.length === 0) {

        categoryList.innerHTML =
            `<p>No categories found.</p>`;

        return;
    }


    categories.forEach(
        function (category) {

            const card =
                document.createElement(
                    "div"
                );

            card.className =
                "category-card";


            card.innerHTML = `

                <span class="category-name">
                    ${category.name}
                </span>

                <div class="category-actions">

                    <button
                        class="edit-button"
                        onclick="editCategory(${category.id}, '${category.name}')">

                        Edit

                    </button>

                    <button
                        class="delete-button"
                        onclick="deleteCategory(${category.id})">

                        Delete

                    </button>

                </div>

            `;


            categoryList.appendChild(
                card
            );
        }
    );
}


// =========================
// ADD CATEGORY
// =========================

saveCategoryButton.addEventListener(
    "click",
    async function () {

        const name =
            categoryName.value.trim();


        if (name === "") {

            categoryMessage.textContent =
                "Please enter a category name.";

            return;
        }


        try {

            const response =
                await fetch(
                    CATEGORY_API_URL,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({

                            userId: USER_ID,

                            name: name

                        })
                    }
                );


            const result =
                await response.json();


            if (!response.ok) {

                throw new Error(
                    result.error ||
                    "Failed to add category"
                );
            }


            categoryMessage.textContent =
                "Category added successfully!";


            categoryName.value = "";


            categoryForm.classList.add(
                "hidden"
            );


            loadCategories();


        } catch (error) {

            console.error(
                "Error adding category:",
                error
            );

            categoryMessage.textContent =
                error.message;
        }
    }
);


// =========================
// EDIT CATEGORY
// =========================

async function editCategory(
    categoryId,
    oldName
) {

    const newName =
        prompt(
            "Enter new category name:",
            oldName
        );


    if (newName === null) {

        return;
    }


    const trimmedName =
        newName.trim();


    if (trimmedName === "") {

        alert(
            "Category name cannot be empty."
        );

        return;
    }


    try {

        const response =
            await fetch(
                CATEGORY_API_URL,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        id: categoryId,

                        userId: USER_ID,

                        name: trimmedName

                    })
                }
            );


        const result =
            await response.json();


        if (!response.ok) {

            throw new Error(
                result.error ||
                "Failed to update category"
            );
        }


        alert(
            "Category updated successfully!"
        );


        loadCategories();


    } catch (error) {

        console.error(
            "Error updating category:",
            error
        );

        alert(
            error.message
        );
    }
}


// =========================
// DELETE CATEGORY
// =========================

async function deleteCategory(
    categoryId
) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this category?"
        );


    if (!confirmed) {

        return;
    }


    try {

        const response =
            await fetch(
                `${CATEGORY_API_URL}?id=${categoryId}&userId=${USER_ID}`,
                {
                    method: "DELETE"
                }
            );


        const result =
            await response.json();


        if (!response.ok) {

            throw new Error(
                result.error ||
                "Failed to delete category"
            );
        }


        alert(
            "Category deleted successfully!"
        );


        loadCategories();


    } catch (error) {

        console.error(
            "Error deleting category:",
            error
        );

        alert(
            error.message
        );
    }
}


// =========================
// INITIAL LOAD
// =========================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadCategories();

    }
);