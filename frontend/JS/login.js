const loginForm =
    document.getElementById("loginForm");

const message =
    document.getElementById("message");


loginForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const email =
            document.getElementById("email")
                .value
                .trim();


        const password =
            document.getElementById("password")
                .value;


        // =========================
        // VALIDATION
        // =========================

        if (email === "") {

            message.textContent =
                "Please enter your email.";

            return;
        }


        if (password === "") {

            message.textContent =
                "Please enter your password.";

            return;
        }


        // =========================
        // LOGIN API
        // =========================

        try {

            const response =
                await fetch(
                    "http://localhost:8080/api/login",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({
                            email: email,
                            password: password
                        })
                    }
                );


            const result =
                await response.json();


            console.log(
                "Login response:",
                result
            );


            if (!response.ok) {

                message.textContent =
                    result.error ||
                    "Invalid email or password.";

                return;
            }


            // =========================
            // SAVE USER ID
            // =========================

            localStorage.setItem(
                "userId",
                result.userId
            );


            localStorage.setItem(
                "userEmail",
                email
            );


            // =========================
            // SUCCESS
            // =========================

            message.textContent =
                "Login successful!";


            // Go to dashboard

            window.location.href =
                "dashboard.html";


        } catch (error) {

            console.error(
                "Login error:",
                error
            );


            message.textContent =
                "Unable to connect to server.";
        }
    }
);