document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.add-to-cart-btn').forEach(button => {
        button.addEventListener('click', function() {
            const bookUuid = this.getAttribute('data-uuid');
            const btn = this;

            btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span>';
            btn.disabled = true;

            fetch('/api/shopping-carts/items', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bookPublicId: bookUuid })
            })
                .then(response => {
                    if (response.ok) {
                        const badge = document.createElement('span');
                        badge.className = "badge rounded-pill bg-success p-2 px-3";
                        badge.innerHTML = '<i class="bi bi-cart-check-fill me-1"></i> In Cart';
                        btn.parentNode.replaceChild(badge, btn);
                    } else {
                        alert("Could not add book. Please try again.");
                        btn.innerHTML = 'Add to Cart';
                        btn.disabled = false;
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    btn.disabled = false;
                });
        });
    });
});