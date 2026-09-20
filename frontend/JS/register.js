const registerForm =
    document.getElementById("registerForm");

const message =
    document.getElementById("message");


registerForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const name =
            document.getElementById("name")
                .value
                .trim();

        const email =
            document.getElementById("email")
                .value
                .trim();

        const password =
            document.getElementById("password")
                .value;

        const confirmPassword =
            document.getElementById("confirmPassword")
                .value;


        // =========================
        // VALIDATION
        // =========================

        if (name === "") {

            message.textContent =
                "Please enter your name.";

            return;
        }


        if (email === "") {

            message.textContent =
                "Please enter your email.";

            return;
        }


        if (password.length < 6) {

            message.textContent =
                "Password must be at least 6 characters.";

            return;
        }


        if (password !== confirmPassword) {

            message.textContent =
                "Passwords do not match.";

            return;
        }


        // =========================
        // REGISTER API
        // =========================

        try {

            const response =
                await fetch(
                    "http://localhost:8080/api/register",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({
                            name: name,
                            email: email,
                            password: password
                        })
                    }
                );


            const result =
                await response.json();


            console.log(
                "Registration response:",
                result
            );


            if (!response.ok) {

                message.textContent =
                    result.error ||
                    "Registration failed.";

                return;
            }


            // =========================
            // SUCCESS
            // =========================

            message.textContent =
                "Registration successful!";


            // Redirect to login

            setTimeout(
                function () {

                    window.location.href =
                        "login.html";

                },
                1000
            );


        } catch (error) {

            console.error(
                "Registration error:",
                error
            );


            message.textContent =
                "Unable to connect to server.";
        }
    }
);