/* Telestaff SDLC Portal — App JS */

// Toast helper
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const id = 'toast-' + Date.now();
    const icons = { success: 'check2-circle', error: 'x-circle', info: 'info-circle' };
    const classes = { success: 'text-bg-success', error: 'text-bg-danger', info: 'text-bg-primary' };

    const html = `
        <div id="${id}" class="toast align-items-center ${classes[type] || 'text-bg-dark'} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-${icons[type] || 'info-circle'} me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>`;
    container.insertAdjacentHTML('beforeend', html);
    const toastEl = document.getElementById(id);
    const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

// Keyboard shortcut: / to focus search
document.addEventListener('keydown', e => {
    if (e.key === '/' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
        e.preventDefault();
        const searchInput = document.querySelector('.ukg-search-input');
        if (searchInput) searchInput.focus();
    }
});

// Auto-resize textareas
document.querySelectorAll('.ukg-textarea').forEach(ta => {
    ta.addEventListener('input', function () {
        this.style.height = 'auto';
        this.style.height = Math.min(this.scrollHeight, 400) + 'px';
    });
});

// Highlight active nav link
(function () {
    const path = window.location.pathname;
    document.querySelectorAll('.ukg-nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href === path || (href !== '/' && path.startsWith(href))) {
            link.classList.add('active');
        }
    });
})();
