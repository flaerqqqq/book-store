document.addEventListener('DOMContentLoaded', function() {
    const deliverySelect = document.getElementById('deliveryType');
    const addressContainer = document.getElementById('addressContainer');
    const addressInput = document.getElementById('addressInput');

    const cartTotalElement = document.getElementById('cartTotalRaw');
    const deliveryFeeElement = document.getElementById('deliveryFeeDisplay');
    const finalTotalElement = document.getElementById('finalTotalDisplay');

    const baseCartTotal = parseFloat(cartTotalElement.getAttribute('data-base-total'));

    function updateTotals() {
        if (!deliverySelect) return;
        const selectedOption = deliverySelect.options[deliverySelect.selectedIndex];
        const cost = parseFloat(selectedOption.getAttribute('data-cost') || 0);
        const type = deliverySelect.value;

        // 1. Update Delivery Fee Display
        deliveryFeeElement.innerText = '+$' + cost.toFixed(2);

        // 2. Calculate and Update Final Total
        const finalTotal = baseCartTotal + cost;
        finalTotalElement.innerText = '$' + finalTotal.toFixed(2);

        // 3. Handle Pickup Logic
        if (type === 'PICKUP') {
            addressContainer.style.opacity = '0.4';
            addressInput.value = "";
            addressInput.readOnly = true;
            addressInput.placeholder = "Not required for pickup";
        } else {
            addressContainer.style.opacity = '1';
            addressInput.readOnly = false;
            if (addressInput.placeholder === "Not required for pickup") {
                addressInput.placeholder = "123 Library St, Booktown";
            }
        }
    }

    deliverySelect.addEventListener('change', updateTotals);
    updateTotals(); // Run once on load
});