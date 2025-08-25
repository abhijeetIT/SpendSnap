/*<![CDATA[*/

    // Global variables for modal state
    let isEditMode = false;
    let currentExpenseId = null;

    // ========== MOBILE MENU FUNCTIONALITY ==========
    function initializeMobileMenu() {
        const mobileMenuToggle = document.getElementById('mobileMenuToggle');
        const navMenu = document.getElementById('navMenu');
        const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');

        if (mobileMenuToggle && navMenu) {
            mobileMenuToggle.addEventListener('click', function(e) {
                e.stopPropagation();
                toggleMobileMenu();
            });
        }

        // Close mobile menu when clicking outside
        document.addEventListener('click', function(e) {
            if (mobileMenuToggle && navMenu &&
                !mobileMenuToggle.contains(e.target) && !navMenu.contains(e.target)) {
                closeMobileMenu();
            }
        });

        // Close mobile menu when clicking overlay
        if (mobileMenuOverlay) {
            mobileMenuOverlay.addEventListener('click', closeMobileMenu);
        }

        // Close mobile menu on window resize to desktop
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) {
                closeMobileMenu();
            }
        });
    }

    function toggleMobileMenu() {
        const navMenu = document.getElementById('navMenu');
        const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');
        const mobileMenuToggle = document.getElementById('mobileMenuToggle');

        if (navMenu && navMenu.classList.contains('active')) {
            closeMobileMenu();
        } else {
            navMenu.classList.add('active');
            if (mobileMenuOverlay) {
                mobileMenuOverlay.classList.add('active');
            }
            // Change hamburger to X
            if (mobileMenuToggle) {
                mobileMenuToggle.innerHTML = '<i class="fas fa-times"></i>';
            }
        }
    }

    function closeMobileMenu() {
        const navMenu = document.getElementById('navMenu');
        const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');
        const mobileMenuToggle = document.getElementById('mobileMenuToggle');

        if (navMenu) navMenu.classList.remove('active');
        if (mobileMenuOverlay) mobileMenuOverlay.classList.remove('active');
        // Change X back to hamburger
        if (mobileMenuToggle) {
            mobileMenuToggle.innerHTML = '<i class="fas fa-bars"></i>';
        }
    }

    // ========== MODAL FUNCTIONS ==========
    function openExpenseModal() {
        resetModalForAdd();
        const modal = document.getElementById('expenseModal');
        modal.classList.add('show');

        // Set focus to first input for accessibility
        setTimeout(() => {
            document.getElementById('expenseAmount').focus();
        }, 300);
    }

    function openEditExpenseModal(button) {
        // Get data from button attributes
        const expenseId = button.getAttribute('data-id');
        const amount = button.getAttribute('data-amount');
        const description = button.getAttribute('data-description') || '';
        const date = button.getAttribute('data-date');
        const categoryId = button.getAttribute('data-category-id');

        // Set edit mode
        setModalForEdit(expenseId, amount, description, date, categoryId);

        const modal = document.getElementById('expenseModal');
        modal.classList.add('show');

        // Set focus to first input for accessibility
        setTimeout(() => {
            document.getElementById('expenseAmount').focus();
        }, 300);
    }

    function resetModalForAdd() {
        isEditMode = false;
        currentExpenseId = null;

        // Update modal appearance for add mode
        document.getElementById('modalTitleText').textContent = 'Add New Expense';
        document.getElementById('modalIcon').className = 'fas fa-plus-circle';
        document.getElementById('submitText').textContent = 'Add Expense';
        document.getElementById('submitIcon').className = 'fas fa-plus-circle';
        document.getElementById('expenseSubmitBtn').className = 'btn btn-submit';

        // Update form action
        document.getElementById('expenseForm').action = '/user/expenses/add';

        // Clear form
        clearForm();

        // Set default date to today
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('expenseDate').value = today;

        // Clear validation
        clearValidationErrors();
    }

    function setModalForEdit(expenseId, amount, description, date, categoryId) {
        isEditMode = true;
        currentExpenseId = expenseId;

        // Update modal appearance for edit mode
        document.getElementById('modalTitleText').textContent = 'Edit Expense';
        document.getElementById('modalIcon').className = 'fas fa-edit';
        document.getElementById('submitText').textContent = 'Update Expense';
        document.getElementById('submitIcon').className = 'fas fa-save';
        document.getElementById('expenseSubmitBtn').className = 'btn btn-update';

        // Update form action for edit
        document.getElementById('expenseForm').action = `/user/expenses/update/${expenseId}`;

        // Populate form with existing data
        document.getElementById('expenseId').value = expenseId;
        document.getElementById('expenseAmount').value = amount;
        document.getElementById('expenseDescription').value = description;
        document.getElementById('expenseDate').value = date;
        document.getElementById('expenseCategory').value = categoryId || '';

        // Clear validation
        clearValidationErrors();
    }

    function clearForm() {
        document.getElementById('expenseId').value = '';
        document.getElementById('expenseAmount').value = '';
        document.getElementById('expenseDescription').value = '';
        document.getElementById('expenseDate').value = '';
        document.getElementById('expenseCategory').value = '';
    }

    function closeExpenseModal() {
        const modal = document.getElementById('expenseModal');
        modal.classList.add('closing');

        setTimeout(() => {
            modal.classList.remove('show', 'closing');
            clearValidationErrors();
        }, 300);
    }

    // Close modal when clicking outside
    window.onclick = function(event) {
        const expenseModal = document.getElementById('expenseModal');
        if (event.target == expenseModal) {
            closeExpenseModal();
        }
    }

    // Close modal with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            const modal = document.getElementById('expenseModal');
            if (modal.classList.contains('show')) {
                closeExpenseModal();
            }
        }
    });

    // ========== FORM VALIDATION & SUBMISSION HANDLERS ==========
    const forms = document.querySelectorAll('form[novalidate]');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
                form.classList.add('was-validated');

                // Focus on first invalid field
                const firstInvalid = form.querySelector(':invalid');
                if (firstInvalid) {
                    firstInvalid.focus();
                }
            } else {
                const submitBtn = form.querySelector('button[type="submit"]');
                if (submitBtn) {
                    submitBtn.disabled = true;
                    const btnText = submitBtn.querySelector('.btn-text');
                    const btnIcon = submitBtn.querySelector('i');

                    if (btnText && btnIcon) {
                        btnText.textContent = 'Processing...';
                        btnIcon.className = 'fas fa-spinner fa-spin';
                    }
                }
            }
        });
    });

    // ========== HELPER FUNCTIONS ==========
    function clearValidationErrors() {
        const form = document.getElementById('expenseForm');
        if (form) {
            form.classList.remove('was-validated');

            const errorElements = form.querySelectorAll('.is-invalid');
            errorElements.forEach(el => {
                el.classList.remove('is-invalid');
            });

            // Reset submit button state
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = false;
                const btnText = submitBtn.querySelector('.btn-text');
                const btnIcon = submitBtn.querySelector('i');

                if (isEditMode) {
                    if (btnText) btnText.textContent = 'Update Expense';
                    if (btnIcon) btnIcon.className = 'fas fa-save';
                } else {
                    if (btnText) btnText.textContent = 'Add Expense';
                    if (btnIcon) btnIcon.className = 'fas fa-plus-circle';
                }
            }
        }
    }

    // ========== ALERT HANDLING ==========
    // Show alerts from server
    const alert = document.querySelector('.alert');
    if (alert) {
        setTimeout(() => {
            alert.style.animation = 'fadeOut 0.5s forwards';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    }

    function showAlert(message, type) {
        const alertHTML = `
            <div class="alert alert-${type}">
                <i class="fas fa-${type === 'success' ? 'check' : 'exclamation'}-circle"></i>
                ${message}
            </div>
        `;

        const alertContainer = document.querySelector('.alert-container');
        if (alertContainer) {
            alertContainer.insertAdjacentHTML('afterbegin', alertHTML);

            setTimeout(() => {
                const newAlert = alertContainer.querySelector('.alert');
                if (newAlert) {
                    newAlert.style.animation = 'fadeOut 0.5s forwards';
                    setTimeout(() => newAlert.remove(), 500);
                }
            }, 5000);
        }
    }

    // ========== DELETE FUNCTION ==========
    async function confirmDeleteExpense(button) {
        if (!confirm('Are you sure you want to permanently delete this expense?')) return;

        const id = button.getAttribute('data-id');
        const icon = button.querySelector('i');
        const originalIcon = icon.className;

        // Show spinner
        icon.className = 'fas fa-spinner fa-spin';
        button.disabled = true;

        try {
            const response = await fetch(`/user/transaction/expenses/delete/${id}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                }
            });

            const result = await response.json();

            // Show alert
            showAlert(result.message, result.status);

            // Remove table row if successful
            if (result.status === 'success') {
                const row = button.closest('tr');
                if (row) {
                    row.style.transition = 'all 0.5s ease';
                    row.style.transform = 'translateX(-100%)';
                    row.style.opacity = '0';
                    setTimeout(() => row.remove(), 500);
                }
            }
        } catch (error) {
            console.error("Delete failed:", error);
            showAlert('Delete failed. Please try again.', 'danger');
        } finally {
            // Reset button
            if (icon) icon.className = originalIcon;
            if (button) button.disabled = false;
        }
    }

    // ========== FILTER FUNCTIONALITY ==========
    document.getElementById('category-filter').addEventListener('change', applyFilters);
    document.getElementById('date-filter').addEventListener('change', applyFilters);

    function applyFilters() {
        const categoryId = document.getElementById('category-filter').value;
        const dateFilter = document.getElementById('date-filter').value;

        let url = '/user/transactions?';
        if(categoryId !== 'all') url += `categoryId=${categoryId}&`;
        if(dateFilter !== 'all') url += `dateFilter=${dateFilter}&`;

        // Remove trailing & or ?
        url = url.slice(0, -1);
        window.location.href = url;
    }

    // ========== INITIALIZE ON PAGE LOAD ==========
    document.addEventListener('DOMContentLoaded', function() {
        const today = new Date().toISOString().split('T')[0];

        // Set max date for date input (no future dates)
        const expenseDateInput = document.getElementById('expenseDate');
        if (expenseDateInput) {
            expenseDateInput.max = today;
        }

        // Initialize mobile menu
        initializeMobileMenu();

        // Add smooth scrolling for pagination
        const pageLinks = document.querySelectorAll('.page-link');
        pageLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                // Add loading state to clicked link
                const icon = this.querySelector('i');
                if (icon) {
                    const originalClass = icon.className;
                    icon.className = 'fas fa-spinner fa-spin';

                    // Reset after a delay (page will change anyway)
                    setTimeout(() => {
                        icon.className = originalClass;
                    }, 1000);
                }
            });
        });

        // Enhanced form interactions
        const amountInput = document.getElementById('expenseAmount');
        if (amountInput) {
            amountInput.addEventListener('input', function() {
                // Remove any non-numeric characters except decimal point
                this.value = this.value.replace(/[^0-9.]/g, '');
            });
        }

        // Auto-focus management for better UX
        const descriptionInput = document.getElementById('expenseDescription');
        if (descriptionInput) {
            descriptionInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    document.getElementById('expenseDate').focus();
                }
            });
        }
    });

    // ========== ACCESSIBILITY IMPROVEMENTS ==========
    // Trap focus in modal
    function trapFocus(modal) {
        const focusableElements = modal.querySelectorAll(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        modal.addEventListener('keydown', function(e) {
            if (e.key === 'Tab') {
                if (e.shiftKey) {
                    if (document.activeElement === firstElement) {
                        e.preventDefault();
                        lastElement.focus();
                    }
                } else {
                    if (document.activeElement === lastElement) {
                        e.preventDefault();
                        firstElement.focus();
                    }
                }
            }
        });
    }

    // Apply focus trap when modal opens
    const modal = document.getElementById('expenseModal');
    if (modal) {
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.attributeName === 'class') {
                    if (modal.classList.contains('show')) {
                        trapFocus(modal);
                    }
                }
            });
        });

        observer.observe(modal, {
            attributes: true,
            attributeFilter: ['class']
        });
    }

    /*]]>*/