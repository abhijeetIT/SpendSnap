document.addEventListener('DOMContentLoaded', function () {
    // ═══════════════════════════════════════════════════════════════
    // JAVASCRIPT – Frontend only collects & sends. Backend decides.
    // ═══════════════════════════════════════════════════════════════

    // Get CSRF tokens from meta tags
    const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // ── Element References ──
    const form                 = document.getElementById('signupForm');
    const emailInput           = document.getElementById('email');
    const emailIcon            = document.getElementById('emailIcon');
    const emailValidMsg        = document.getElementById('emailValid');
    const verifyEmailBtn       = document.getElementById('verifyEmailBtn');
    const otpSection           = document.getElementById('otpSection');
    const otpBoxes             = document.querySelectorAll('.otp-box');
    const otpFeedback          = document.getElementById('otpFeedback');
    const otpTimer             = document.getElementById('otpTimer');
    const resendBtn            = document.getElementById('resendBtn');
    const nameInput            = document.getElementById('name');
    const password             = document.getElementById('password');
    const togglePassword       = document.getElementById('togglePassword');
    const confirmPassword      = document.getElementById('confirmPassword');
    const toggleConfirmPassword= document.getElementById('toggleConfirmPassword');
    const confirmPasswordError = document.getElementById('confirmPasswordError');
    const confirmPasswordValid = document.getElementById('confirmPasswordValid');
    const passwordStrengthBar  = document.getElementById('passwordStrengthBar');
    const submitBtn            = document.getElementById('submitBtn');

    // ── State Variables ──
    let emailVerified = false;
    let resendCooldown = 0;
    let resendInterval = null;

    // ════════════════════════════════════════
    // EMAIL VALIDATION (Format Only)
    // ════════════════════════════════════════
    function isValidEmail(val) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
    }

    function validateEmail() {
        const val = emailInput.value.trim();

        if (val && isValidEmail(val)) {
            emailInput.classList.remove('is-invalid');
            emailInput.classList.add('is-valid');
            emailIcon.className   = 'fas fa-check input-icon';
            emailIcon.style.color = 'var(--success)';
            emailValidMsg.style.display = 'block';
            verifyEmailBtn.disabled = false;
            return true;
        } else if (val) {
            emailInput.classList.add('is-invalid');
            emailInput.classList.remove('is-valid');
            emailIcon.className   = 'fas fa-times input-icon';
            emailIcon.style.color = 'var(--danger)';
            emailValidMsg.style.display = 'none';
            verifyEmailBtn.disabled = true;
            return false;
        } else {
            emailInput.classList.remove('is-invalid', 'is-valid');
            emailIcon.className   = 'fas fa-envelope input-icon';
            emailIcon.style.color = 'var(--gray-400)';
            emailValidMsg.style.display = 'none';
            verifyEmailBtn.disabled = true;
            return true;
        }
    }

    // Run validation on page load in case of autofill
    validateEmail();

    emailInput.addEventListener('input', function () {
        validateEmail();
        // Reset verification if email changes
        if (emailVerified) {
            emailVerified = false;
            submitBtn.disabled = true;
            hideOTP();
            setOTPFeedback('', '');
        }
    });

    // ════════════════════════════════════════
    // SEND OTP - POST /auth/send-otp
    // ════════════════════════════════════════
    verifyEmailBtn.addEventListener('click', function() {
        verifyEmailBtn.classList.add('loading');
        verifyEmailBtn.disabled = true;

        fetch('/auth/send-otp', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ email: emailInput.value.trim() })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                // ✅ Only show OTP section if backend successfully sent OTP
                showOTP();
                startResendCooldown(data.expiresIn || 120);
            } else {
                // ❌ Backend error (email exists, invalid email, etc.) - show alert, keep button enabled
                verifyEmailBtn.disabled = false;
                alert(data.message || 'Failed to send OTP. Please try again.');
            }
        })
        .catch(err => {
            // ❌ Network error - re-enable button
            verifyEmailBtn.disabled = false;
            console.error(err);
            alert('Network error. Please try again.');
        })
        .finally(() => {
            verifyEmailBtn.classList.remove('loading');
        });
    });

    // ════════════════════════════════════════
    // OTP UI Functions
    // ════════════════════════════════════════
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
        otpFeedback.className   = 'otp-feedback' + (type ? ' ' + type : '');
    }

    // ════════════════════════════════════════
    // OTP INPUT HANDLING (4-digit boxes)
    // ════════════════════════════════════════
    otpBoxes.forEach((box, idx) => {
        box.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 1);

            if (this.value) {
                this.classList.add('filled');
                if (idx < otpBoxes.length - 1) otpBoxes[idx + 1].focus();
            } else {
                this.classList.remove('filled');
            }

            if (allBoxesFilled()) verifyOTP();
        });

        box.addEventListener('keydown', function (e) {
            if (e.key === 'Backspace' && !this.value && idx > 0) {
                otpBoxes[idx - 1].focus();
                otpBoxes[idx - 1].value = '';
                otpBoxes[idx - 1].classList.remove('filled');
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

        box.addEventListener('focus', function () { this.select(); });
    });

    function allBoxesFilled() {
        return Array.from(otpBoxes).every(b => b.value.length === 1);
    }

    function getOTPValue() {
        return Array.from(otpBoxes).map(b => b.value).join('');
    }

    // ════════════════════════════════════════
    // VERIFY OTP - POST /auth/verify-otp
    // ════════════════════════════════════════
    let isVerifying = false;

    function verifyOTP() {
        if (isVerifying) return;
        isVerifying = true;

        otpBoxes.forEach(b => b.disabled = true);
        setOTPFeedback('Verifying OTP...', '');

        fetch('/auth/verify-otp', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
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
                emailVerified = true;
                otpBoxes.forEach(b => {
                    b.classList.add('is-valid');
                    b.disabled = true;
                });
                setOTPFeedback('Email verified successfully!', 'success');
                clearResendTimer();
                verifyEmailBtn.disabled = true;
                submitBtn.disabled = false;
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

    // ════════════════════════════════════════
    // RESEND TIMER
    // ════════════════════════════════════════
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

    resendBtn.addEventListener('click', function () {
        resendBtn.disabled = true;
        resetOTPBoxes();
        setOTPFeedback('', '');
        emailVerified = false;
        submitBtn.disabled = true;

        fetch('/auth/send-otp', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ email: emailInput.value.trim() })
        })
        .then(res => res.json())
        .catch(() => ({ success: false, message: 'Network error.' }))
        .then(data => {
            if (data.success) {
                setOTPFeedback('New OTP sent!', 'success');
                startResendCooldown(data.expiresIn || 120);
                otpBoxes[0].focus();
            } else {
                setOTPFeedback(data.message || 'Failed to resend OTP.', 'error');
                resendBtn.disabled = false;
            }
        });
    });

    // ════════════════════════════════════════
    // PASSWORD TOGGLE
    // ════════════════════════════════════════
    function handlePasswordToggle(input, toggle) {
        const isPwd = input.type === 'password';
        input.type = isPwd ? 'text' : 'password';
        toggle.classList.toggle('fa-eye-slash', isPwd);
        toggle.classList.toggle('fa-eye', !isPwd);
    }

    togglePassword.addEventListener('click', () => handlePasswordToggle(password, togglePassword));
    toggleConfirmPassword.addEventListener('click', () => handlePasswordToggle(confirmPassword, toggleConfirmPassword));

    // ════════════════════════════════════════
    // PASSWORD STRENGTH
    // ════════════════════════════════════════
    function checkPasswordStrength(val) {
        let strength = 0;
        if (val.length >= 8)            strength++;
        if (/[a-z]/.test(val))          strength++;
        if (/[A-Z]/.test(val))          strength++;
        if (/[0-9]/.test(val))          strength++;
        if (/[^A-Za-z0-9]/.test(val))   strength++;
        return strength;
    }

    password.addEventListener('input', function () {
        const s = checkPasswordStrength(this.value);
        passwordStrengthBar.className = 'password-strength-bar';
        if (this.value) {
            if      (s <= 2) passwordStrengthBar.classList.add('strength-weak');
            else if (s === 3) passwordStrengthBar.classList.add('strength-fair');
            else if (s === 4) passwordStrengthBar.classList.add('strength-good');
            else              passwordStrengthBar.classList.add('strength-strong');
        }
        validateConfirmPassword();
    });

    // ════════════════════════════════════════
    // CONFIRM PASSWORD
    // ════════════════════════════════════════
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

    confirmPassword.addEventListener('input', validateConfirmPassword);

    // ════════════════════════════════════════
    // NAME VALIDATION
    // ════════════════════════════════════════
    function validateName() {
        if (nameInput.value && nameInput.value.trim().length >= 2) {
            nameInput.classList.remove('is-invalid');
            nameInput.classList.add('is-valid');
            return true;
        } else if (nameInput.value) {
            nameInput.classList.add('is-invalid');
            nameInput.classList.remove('is-valid');
            return false;
        } else {
            nameInput.classList.remove('is-invalid', 'is-valid');
        }
        return true;
    }

    nameInput.addEventListener('input', validateName);

    // ════════════════════════════════════════
    // FORM SUBMISSION
    // ════════════════════════════════════════
    form.addEventListener('submit', function (e) {
        let isValid = true;

        if (!validateName())             isValid = false;
        if (!validateEmail())            isValid = false;
        if (!validateConfirmPassword())  isValid = false;

        if (checkPasswordStrength(password.value) < 3) {
            password.classList.add('is-invalid');
            isValid = false;
        }

        if (!emailVerified) {
            setOTPFeedback('Please verify your email with OTP before signing up.', 'error');
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

    // ════════════════════════════════════════
    // FOCUS MICRO-INTERACTIONS
    // ════════════════════════════════════════
    document.querySelectorAll('.form-control').forEach(input => {
        input.addEventListener('focus', function () {
            this.parentElement.style.transform = 'translateY(-1px)';
        });
        input.addEventListener('blur', function () {
            this.parentElement.style.transform = 'translateY(0)';
        });
    });
});