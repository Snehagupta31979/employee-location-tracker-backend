document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    const alertBox = document.getElementById('loginAlert');
    const loginBtn = document.getElementById('loginBtn');
    const loginBtnText = document.getElementById('loginBtnText');
    const loginSpinner = document.getElementById('loginSpinner');

    // If already logged in, skip straight to dashboard
    Api.get('/api/auth/me').then(() => {
        window.location.href = 'dashboard.html';
    }).catch(() => { /* not logged in - stay on login page */ });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertBox.classList.add('d-none');

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        loginBtn.disabled = true;
        loginBtnText.classList.add('d-none');
        loginSpinner.classList.remove('d-none');

        try {
            await Api.post('/api/auth/login', { username, password });
            window.location.href = 'dashboard.html';
        } catch (err) {
            alertBox.textContent = err.message || 'Login failed. Please check your credentials.';
            alertBox.classList.remove('d-none');
        } finally {
            loginBtn.disabled = false;
            loginBtnText.classList.remove('d-none');
            loginSpinner.classList.add('d-none');
        }
    });

    /* =========================================================
       REGISTER / LOGIN VIEW TOGGLE
       ========================================================= */
    const loginWrapper = document.getElementById('loginWrapper');
    const registerWrapper = document.getElementById('registerWrapper');
    const showRegisterLink = document.getElementById('showRegisterLink');
    const backToLoginLink = document.getElementById('backToLoginLink');
    const registerForm = document.getElementById('registerForm');
    const registerAlert = document.getElementById('registerAlert');
    const registerSuccessAlert = document.getElementById('registerSuccessAlert');
    const registerBtn = document.getElementById('registerBtn');
    const registerBtnText = document.getElementById('registerBtnText');
    const registerSpinner = document.getElementById('registerSpinner');

    if (showRegisterLink) {
        showRegisterLink.addEventListener('click', (e) => {
            e.preventDefault();
            loginWrapper.classList.add('d-none');
            registerWrapper.classList.remove('d-none');
            registerAlert.classList.add('d-none');
            registerSuccessAlert.classList.add('d-none');
        });
    }

    if (backToLoginLink) {
        backToLoginLink.addEventListener('click', (e) => {
            e.preventDefault();
            registerWrapper.classList.add('d-none');
            loginWrapper.classList.remove('d-none');
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            registerAlert.classList.add('d-none');
            registerSuccessAlert.classList.add('d-none');

            const fullName = document.getElementById('regFullName').value.trim();
            const username = document.getElementById('regUsername').value.trim();
            const email = document.getElementById('regEmail').value.trim();
            const password = document.getElementById('regPassword').value;
            const confirmPassword = document.getElementById('regConfirmPassword').value;

            if (password !== confirmPassword) {
                registerAlert.textContent = 'Passwords do not match.';
                registerAlert.classList.remove('d-none');
                return;
            }

            registerBtn.disabled = true;
            registerBtnText.classList.add('d-none');
            registerSpinner.classList.remove('d-none');

            try {
                await Api.post('/api/auth/register', { fullName, username, email, password });

                registerSuccessAlert.textContent = 'Registration successful! You can now sign in.';
                registerSuccessAlert.classList.remove('d-none');
                registerForm.reset();

                setTimeout(() => {
                    registerWrapper.classList.add('d-none');
                    loginWrapper.classList.remove('d-none');
                    document.getElementById('username').value = username;
                    document.getElementById('password').focus();
                }, 1200);
            } catch (err) {
                registerAlert.textContent = err.message || 'Registration failed. Please try again.';
                registerAlert.classList.remove('d-none');
            } finally {
                registerBtn.disabled = false;
                registerBtnText.classList.remove('d-none');
                registerSpinner.classList.add('d-none');
            }
        });
    }
});

async function logout() {
    try {
        await Api.post('/api/auth/logout');
    } catch (e) { /* ignore */ }
    window.location.href = 'index.html';
}