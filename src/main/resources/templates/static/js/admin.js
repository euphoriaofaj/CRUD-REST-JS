class AdminPanel {
    constructor() {
        this.users = [];
        this.roles = [];
        this.currentEditingUserId = null;
        this.init();
    }

    async init() {
        await this.loadRoles();
        await this.loadUsers();
        this.bindEvents();
    }

    async loadUsers() {
        try {
            const response = await fetch('/api/users');
            if (response.ok) {
                this.users = await response.json();
                this.renderUsersTable();
            } else {
                this.showAlert('Error loading users', 'danger');
            }
        } catch (error) {
            this.showAlert('Error loading users', 'danger');
        }
    }

    async loadRoles() {
        try {
            const response = await fetch('/api/users/roles');
            if (response.ok) {
                this.roles = await response.json();
                this.renderRoleOptions();
            } else {
                this.showAlert('Error loading roles', 'danger');
            }
        } catch (error) {
            this.showAlert('Error loading roles', 'danger');
        }
    }

    renderUsersTable() {
        const tbody = document.querySelector('#usersTable tbody');
        if (!tbody) return;
        tbody.innerHTML = '';
        this.users.forEach(user => {
            const row = document.createElement('tr');
            const rolesText = user.roles ? user.roles.map(role => `[${role.name}]`).join(' ') : '';
            row.innerHTML = `
                <td>${user.id}</td>
                <td>${user.firstName || ''}</td>
                <td>${user.lastName || ''}</td>
                <td>${user.age || ''}</td>
                <td>${user.email || ''}</td>
                <td>${rolesText}</td>
                <td>
                    <button class="btn btn-info btn-sm" onclick="adminPanel.editUser(${user.id})">Edit</button>
                </td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="adminPanel.deleteUser(${user.id})">Delete</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    renderRoleOptions() {
        const selects = document.querySelectorAll('.role-select');
        selects.forEach(select => {
            select.innerHTML = '';
            this.roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role.id;
                option.textContent = role.name;
                select.appendChild(option);
            });
        });
    }

    bindEvents() {
        const newUserForm = document.getElementById('newUserForm');
        if (newUserForm) {
            newUserForm.addEventListener('submit', (e) => this.handleUserSubmit(e));
        }

        const editUserForm = document.getElementById('editUserForm');
        if (editUserForm) {
            editUserForm.addEventListener('submit', (e) => this.handleUserSubmit(e, true));
        }

        const newUserTab = document.getElementById('newUserTab');
        if (newUserTab) {
            newUserTab.addEventListener('click', () => this.showNewUserForm());
        }

        const allUsersTab = document.getElementById('allUsersTab');
        if (allUsersTab) {
            allUsersTab.addEventListener('click', () => this.showUsersTable());
        }
    }

    async handleUserSubmit(event, isEdit = false) {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);
        const userData = {
            firstName: formData.get('firstName'),
            lastName: formData.get('lastName'),
            age: formData.get('age') ? parseInt(formData.get('age')) : null,
            email: formData.get('email'),
            username: formData.get('username'),
            password: formData.get('password'),
            roleIds: formData.getAll('roleIds')
        };

        try {
            let response;
            if (isEdit && this.currentEditingUserId) {
                response = await fetch(`/api/users/${this.currentEditingUserId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(userData)
                });
            } else {
                response = await fetch('/api/users', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(userData)
                });
            }

            const result = await response.json();

            if (result.success) {
                this.showAlert(result.message, 'success');
                await this.loadUsers();
                form.reset();
                if (isEdit) {
                    const modal = bootstrap.Modal.getInstance(document.getElementById('editModal'));
                    modal.hide();
                } else {
                    this.showUsersTable();
                }
            } else {
                this.showAlert(result.message, 'danger');
            }
        } catch (error) {
            this.showAlert('Error saving user', 'danger');
        }
    }

    async editUser(userId) {
        const user = this.users.find(u => u.id === userId);
        if (!user) return;
        this.currentEditingUserId = userId;
        document.getElementById('editId').value = user.id;
        document.getElementById('editIdDisplay').value = user.id;
        document.getElementById('editFirstName').value = user.firstName || '';
        document.getElementById('editLastName').value = user.lastName || '';
        document.getElementById('editAge').value = user.age || '';
        document.getElementById('editEmail').value = user.email || '';
        document.getElementById('editUsername').value = user.username || '';
        document.getElementById('editPassword').value = '';
        const roleSelect = document.getElementById('editRole');
        Array.from(roleSelect.options).forEach(option => {
            option.selected = user.roles && user.roles.some(role => role.id == option.value);
        });
        const modal = new bootstrap.Modal(document.getElementById('editModal'));
        modal.show();
    }

    async deleteUser(userId) {
        const user = this.users.find(u => u.id === userId);
        if (!user) return;
        document.getElementById('deleteIdDisplay').value = user.id;
        document.getElementById('deleteFirstName').value = user.firstName || '';
        document.getElementById('deleteLastName').value = user.lastName || '';
        document.getElementById('deleteAge').value = user.age || '';
        document.getElementById('deleteEmail').value = user.email || '';
        document.getElementById('deleteUsername').value = user.username || '';
        const rolesText = user.roles ? user.roles.map(role => `[${role.name}]`).join(' ') : '';
        document.getElementById('deleteRole').value = rolesText;
        document.getElementById('confirmDelete').onclick = () => this.confirmDelete(userId);
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    }

    async confirmDelete(userId) {
        try {
            const response = await fetch(`/api/users/${userId}`, {
                method: 'DELETE'
            });
            const result = await response.json();
            if (result.success) {
                this.showAlert(result.message, 'success');
                await this.loadUsers();
                const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
                modal.hide();
            } else {
                this.showAlert(result.message, 'danger');
            }
        } catch (error) {
            this.showAlert('Error deleting user', 'danger');
        }
    }

    showNewUserForm() {
        document.getElementById('usersTableContainer').style.display = 'none';
        document.getElementById('newUserFormContainer').style.display = 'block';
        document.getElementById('allUsersTab').classList.remove('active');
        document.getElementById('newUserTab').classList.add('active');
    }

    showUsersTable() {
        document.getElementById('newUserFormContainer').style.display = 'none';
        document.getElementById('usersTableContainer').style.display = 'block';
        document.getElementById('newUserTab').classList.remove('active');
        document.getElementById('allUsersTab').classList.add('active');
    }

    showAlert(message, type) {
        const existingAlerts = document.querySelectorAll('.alert');
        existingAlerts.forEach(alert => alert.remove());
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        const mainContent = document.querySelector('.main-content');
        mainContent.insertBefore(alert, mainContent.firstChild);
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 5000);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.adminPanel = new AdminPanel();
});
