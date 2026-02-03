document.addEventListener('DOMContentLoaded', function () {

        //CSRF all
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


        // ===== Element References =====
        const form = document.getElementById('forgotPasswordForm');
        const emailInput = document.getElementById('email');
        const emailIcon = document.getElementById('emailIcon');
        const emailValidMsg = document.getElementById('emailValid');
        const emailErrorMessage = document.getElementById('emailErrorMessage');
        const emailErrorText = document.getElementById('emailErrorText');
        const sendOtpBtn = document.getElementById('sendOtpBtn');
        const otpSection = document.getElementById('otpSection');
        const otpBoxes = document.querySelectorAll('.otp-box');
        const otpFeedback = document.getElementById('otpFeedback');
        const otpTimer = document.getElementById('otpTimer');
        const resendBtn = document.getElementById('resendBtn');
        const passwordSection = document.getElementById('passwordSection');
        const confirmPasswordSection = document.getElementById('confirmPasswordSection');
        const password = document.getElementById('password');
        const togglePassword = document.getElementById('togglePassword');
        const confirmPassword = document.getElementById('confirmPassword');
        const toggleConfirmPassword = document.getElementById('toggleConfirmPassword');
        const confirmPasswordError = document.getElementById('confirmPasswordError');
        const confirmPasswordValid = document.getElementById('confirmPasswordValid');
        const passwordStrengthBar = document.getElementById('passwordStrengthBar');
        const submitBtn = document.getElementById('submitBtn');

        // ===== State Variables =====
        let otpVerified = false;
        let resendCooldown = 0;
        let resendInterval = null;

        // ===== EMAIL VALIDATION =====
        function isValidEmail(val) {
            return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
        }

        function validateEmail() {
            const val = emailInput.value.trim();

            if (val && isValidEmail(val)) {
                emailInput.classList.remove('is-invalid');
                emailInput.classList.add('is-valid');
                emailIcon.className = 'fas fa-check input-icon';
                emailIcon.style.color = 'var(--success)';
                emailValidMsg.style.display = 'block';
                emailErrorMessage.classList.remove('show'); // Hide error message
                sendOtpBtn.disabled = false;
                return true;
            } else if (val) {
                emailInput.classList.add('is-invalid');
                emailInput.classList.remove('is-valid');
                emailIcon.className = 'fas fa-times input-icon';
                emailIcon.style.color = 'var(--danger)';
                emailValidMsg.style.display = 'none';
                sendOtpBtn.disabled = true;
                return false;
            } else {
                emailInput.classList.remove('is-invalid', 'is-valid');
                emailIcon.className = 'fas fa-envelope input-icon';
                emailIcon.style.color = 'var(--gray-400)';
                emailValidMsg.style.display = 'none';
                emailErrorMessage.classList.remove('show'); // Hide error message
                sendOtpBtn.disabled = true;
                return true;
            }
        }

        emailInput.addEventListener('input', function () {
            validateEmail();
            if (otpVerified) {
                otpVerified = false;
                submitBtn.disabled = true;
                hidePasswordFields();
                hideOTP();
                setOTPFeedback('', '');
            }
        });

        // ===== SEND OTP (FIXED) =====
        sendOtpBtn.addEventListener('click', function () {
            if (!isValidEmail(emailInput.value.trim())) return;

            sendOtpBtn.classList.add('loading');
            sendOtpBtn.disabled = true;
            emailErrorMessage.classList.remove('show'); // Hide previous error

            fetch('/auth/send-reset-otp', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({ email: emailInput.value.trim() })
            })
            .then(res => res.json())
            .then(data => {
                sendOtpBtn.classList.remove('loading');

                if (data.success) {
                    // ✅ SUCCESS - Email exists, OTP sent
                    emailErrorMessage.classList.remove('show');
                    showOTP();
                    setOTPFeedback(data.message || 'OTP sent to your email!', 'success');
                    startResendCooldown(120);
                } else {
                    // ❌ ERROR - Email not found or other backend error
                    sendOtpBtn.disabled = false;
                    emailErrorText.textContent = data.message;
                    emailErrorMessage.classList.add('show');
                    hideOTP(); // Make sure OTP section is hidden
                }
            })
            .catch(error => {
                sendOtpBtn.classList.remove('loading');
                sendOtpBtn.disabled = false;
                emailErrorText.textContent = 'Network error. Please try again.';
                emailErrorMessage.classList.add('show');
            });
        });

        // ===== OTP UI FUNCTIONS =====
        function showOTP() {
            otpSection.classList.add('visible');
            setTimeout(() => otpBoxes[0].focus(), 100);
        }

        function hideOTP() {
            otpSection.classList.remove('visible');
            resetOTPBoxes();
            clearResendTimer();
        }

        function resetOTPBoxes() {
            otpBoxes.forEach(box => {
                box.value = '';
                box.classList.remove('filled', 'is-valid', 'is-invalid');
                box.disabled = false;
            });
        }

        function setOTPFeedback(msg, type) {
            otpFeedback.textContent = msg;
            otpFeedback.className = 'otp-feedback' + (type ? ' ' + type : '');
        }

        function showPasswordFields() {
            passwordSection.style.display = 'block';
            confirmPasswordSection.style.display = 'block';
        }

        function hidePasswordFields() {
            passwordSection.style.display = 'none';
            confirmPasswordSection.style.display = 'none';
            password.value = '';
            confirmPassword.value = '';
        }

        // ===== OTP INPUT HANDLING =====
        otpBoxes.forEach((box, idx) => {
            box.addEventListener('input', function () {
                this.value = this.value.replace(/\D/g, '').slice(0, 1);

                if (this.value) {
                    this.classList.add('filled');
                    if (idx < otpBoxes.length - 1) {
                        otpBoxes[idx + 1].focus();
                    }
                } else {
                    this.classList.remove('filled');
                }

                if (allBoxesFilled()) {
                    verifyOTP();
                }
            });

            box.addEventListener('keydown', function (e) {
                if (e.key === 'Backspace') {
                    if (!this.value && idx > 0) {
                        otpBoxes[idx - 1].focus();
                        otpBoxes[idx - 1].value = '';
                        otpBoxes[idx - 1].classList.remove('filled');
                    }
                }
            });

            box.addEventListener('paste', function (e) {
                e.preventDefault();
                const digits = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 4);
                digits.split('').forEach((d, i) => {
                    if (otpBoxes[i]) {
                        otpBoxes[i].value = d;
                        otpBoxes[i].classList.add('filled');
                    }
                });
                const nextIdx = Math.min(digits.length, otpBoxes.length - 1);
                otpBoxes[nextIdx].focus();

                if (allBoxesFilled()) verifyOTP();
            });

            box.addEventListener('focus', function () {
                this.select();
            });
        });

        function allBoxesFilled() {
            return Array.from(otpBoxes).every(b => b.value.length === 1);
        }

        function getOTPValue() {
            return Array.from(otpBoxes).map(b => b.value).join('');
        }

        // ===== VERIFY OTP =====
        let isVerifying = false;

        function verifyOTP() {
            if (isVerifying) return;
            isVerifying = true;

            otpBoxes.forEach(b => b.disabled = true);
            setOTPFeedback('Verifying OTP...', '');

            fetch('/auth/verify-reset-otp', {
                method: 'POST',
                headers: {'Content-Type': 'application/json',[csrfHeader]: csrfToken},
                body: JSON.stringify({
                    email: emailInput.value.trim(),
                    otp: getOTPValue()
                })
            })
            .then(res => res.json())
            .catch(() => ({ success: false, message: 'Network error. Please try again.' }))
            .then(data => {
                isVerifying = false;

                if (data.success) {
                    otpVerified = true;
                    otpBoxes.forEach(b => {
                        b.classList.add('is-valid');
                        b.disabled = true;
                    });
                    setOTPFeedback('OTP verified successfully!', 'success');
                    clearResendTimer();
                    sendOtpBtn.disabled = true;
                    showPasswordFields();
                    setTimeout(() => password.focus(), 300);
                } else {
                    otpBoxes.forEach(b => {
                        b.classList.add('is-invalid');
                        b.disabled = false;
                    });
                    setOTPFeedback(data.message || 'Invalid or expired OTP. Please try again.', 'error');

                    setTimeout(() => {
                        resetOTPBoxes();
                        otpBoxes[0].focus();
                    }, 900);
                }
            });
        }

        // ===== RESEND COOLDOWN TIMER =====
        function startResendCooldown(seconds) {
            clearResendTimer();
            resendCooldown = seconds;
            resendBtn.disabled = true;
            updateTimerDisplay();

            resendInterval = setInterval(() => {
                resendCooldown--;
                if (resendCooldown <= 0) {
                    clearResendTimer();
                    resendBtn.disabled = false;
                    otpTimer.textContent = '';
                } else {
                    updateTimerDisplay();
                }
            }, 1000);
        }

        function updateTimerDisplay() {
            const m = Math.floor(resendCooldown / 60);
            const s = resendCooldown % 60;
            otpTimer.textContent = `${m}:${s.toString().padStart(2, '0')}`;
        }

        function clearResendTimer() {
            if (resendInterval) {
                clearInterval(resendInterval);
                resendInterval = null;
            }
            resendCooldown = 0;
            otpTimer.textContent = '';
        }

        // ===== RESEND OTP =====
        resendBtn.addEventListener('click', function () {
            resendBtn.disabled = true;
            resetOTPBoxes();
            setOTPFeedback('', '');
            otpVerified = false;
            submitBtn.disabled = true;
            hidePasswordFields();

            fetch('/auth/send-reset-otp', {
                method: 'POST',
                headers: {'Content-Type': 'application/json',[csrfHeader]: csrfToken},
                body: JSON.stringify({ email: emailInput.value.trim() })
            })
            .then(res => res.json())
            .catch(() => ({ success: false, message: 'Network error.' }))
            .then(data => {
                if (data.success) {
                    setOTPFeedback('New OTP sent to your email!', 'success');
                    startResendCooldown(120);
                    otpBoxes[0].focus();
                } else {
                    setOTPFeedback(data.message || 'Failed to resend OTP.', 'error');
                    resendBtn.disabled = false;
                }
            });
        });

        // ===== PASSWORD TOGGLE =====
        function handlePasswordToggle(input, toggle) {
            const isPwd = input.type === 'password';
            input.type = isPwd ? 'text' : 'password';
            toggle.classList.toggle('fa-eye-slash', !isPwd);
            toggle.classList.toggle('fa-eye', isPwd);
            toggle.style.color = isPwd ? 'var(--primary)' : 'var(--gray-400)';
        }

        togglePassword.addEventListener('click', () => handlePasswordToggle(password, togglePassword));
        toggleConfirmPassword.addEventListener('click', () => handlePasswordToggle(confirmPassword, toggleConfirmPassword));

        // ===== PASSWORD STRENGTH =====
        function checkPasswordStrength(val) {
            let strength = 0;
            if (val.length >= 8) strength++;
            if (/[a-z]/.test(val)) strength++;
            if (/[A-Z]/.test(val)) strength++;
            if (/[0-9]/.test(val)) strength++;
            if (/[^A-Za-z0-9]/.test(val)) strength++;
            return strength;
        }

        password.addEventListener('input', function () {
            const s = checkPasswordStrength(this.value);
            passwordStrengthBar.className = 'password-strength-bar';
            if (this.value) {
                if (s <= 2) passwordStrengthBar.classList.add('strength-weak');
                else if (s === 3) passwordStrengthBar.classList.add('strength-fair');
                else if (s === 4) passwordStrengthBar.classList.add('strength-good');
                else passwordStrengthBar.classList.add('strength-strong');
            }
            validateConfirmPassword();
            checkFormValidity();
        });

        // ===== CONFIRM PASSWORD VALIDATION =====
        function validateConfirmPassword() {
            if (confirmPassword.value && password.value !== confirmPassword.value) {
                confirmPassword.classList.add('is-invalid');
                confirmPassword.classList.remove('is-valid');
                confirmPasswordError.style.display = 'block';
                confirmPasswordValid.style.display = 'none';
                return false;
            } else if (confirmPassword.value && password.value === confirmPassword.value) {
                confirmPassword.classList.remove('is-invalid');
                confirmPassword.classList.add('is-valid');
                confirmPasswordError.style.display = 'none';
                confirmPasswordValid.style.display = 'block';
                return true;
            } else {
                confirmPassword.classList.remove('is-invalid', 'is-valid');
                confirmPasswordError.style.display = 'none';
                confirmPasswordValid.style.display = 'none';
            }
            return true;
        }

        confirmPassword.addEventListener('input', function () {
            validateConfirmPassword();
            checkFormValidity();
        });

        // ===== ENABLE SUBMIT WHEN READY =====
        function checkFormValidity() {
            const passwordValid = password.value && checkPasswordStrength(password.value) >= 3;
            const confirmValid = confirmPassword.value && password.value === confirmPassword.value;
            submitBtn.disabled = !(otpVerified && passwordValid && confirmValid);
        }

        // ===== FORM SUBMISSION =====
        form.addEventListener('submit', function (e) {
            let isValid = true;

            if (!validateEmail()) isValid = false;
            if (checkPasswordStrength(password.value) < 3) {
                password.classList.add('is-invalid');
                isValid = false;
            }
            if (!validateConfirmPassword()) isValid = false;
            if (!otpVerified) {
                setOTPFeedback('Please verify OTP first.', 'error');
                showOTP();
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
                return;
            }

            submitBtn.classList.add('btn-loading');
            submitBtn.disabled = true;
        });

        // ===== FOCUS ANIMATIONS =====
        document.querySelectorAll('.form-control').forEach(input => {
            input.addEventListener('focus', function () {
                this.parentElement.style.transform = 'translateY(-1px)';
            });
            input.addEventListener('blur', function () {
                this.parentElement.style.transform = 'translateY(0)';
            });
        });

        // ===== LOGO ANIMATION =====
        window.addEventListener('load', function () {
            const logo = document.querySelector('.logo');
            setTimeout(() => {
                logo.style.transform = 'scale(1.1) rotate(360deg)';
                setTimeout(() => {
                    logo.style.transform = 'scale(1) rotate(0deg)';
                }, 800);
            }, 500);

            if (!emailInput.value) {
                emailInput.focus();
            }
        });
    });