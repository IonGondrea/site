async function fetchProducts() {
  const res = await fetch('/api/products');
  if (!res.ok) throw new Error('Failed to load products');
  return res.json();
}

async function fetchCart() {
  const res = await fetch('/api/cart');
  if (!res.ok) throw new Error('Failed to load cart');
  return res.json();
}

async function addToCart(productId, qty) {
  try {
    const res = await fetch('/api/cart/add', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({productId, qty})
    });
    if (!res.ok) {
      const err = await res.json().catch(()=>({error:'Unknown error'}));
      throw new Error(err.error || 'Failed to add to cart');
    }
    showMessage('Added to cart', 'success');
    await refreshCart();
  } catch (e) {
    showMessage(e.message, 'error');
  }
}

function showMessage(text, type='info') {
  const el = document.getElementById('message');
  el.className = 'message ' + type;
  el.textContent = text;
  el.classList.remove('hidden');
  clearTimeout(el._t);
  el._t = setTimeout(() => el.classList.add('hidden'), 3500);
}

async function refreshProducts() {
  try {
    const products = await fetchProducts();
    const el = document.getElementById('products');
    el.innerHTML = '';
    products.forEach(p => {
      const imgSrc = p.image || 'images/placeholder.svg';
      const div = document.createElement('div');
      div.className = 'product';
      div.innerHTML = `
        <div class="prod-left">
          <img class="prod-img" src="${escapeHtml(imgSrc)}" alt="${escapeHtml(p.name)}" />
          <div class="prod-name">${escapeHtml(p.name)}</div>
          <div class="prod-price">$${p.price.toFixed(2)}</div>
        </div>
        <div class="prod-right">
          <input class="qty" type="number" min="1" value="1" id="qty-${p.id}" />
          <button class="add" data-id="${p.id}">Add</button>
        </div>
      `;
      el.appendChild(div);
    });
    // attach listeners
    el.querySelectorAll('button.add').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-id');
        const qty = parseInt(document.getElementById('qty-' + id).value, 10) || 1;
        addToCart(parseInt(id, 10), qty);
      });
    });
  } catch (e) {
    showMessage('Could not load products: ' + e.message, 'error');
    document.getElementById('products').innerHTML = '<div class="muted">Failed to load products.</div>';
  }
}

async function refreshCart() {
  try {
    const data = await fetchCart();
    const el = document.getElementById('cart');
    if (!data.items || data.items.length === 0) {
      el.innerText = 'Cart is empty.';
      document.getElementById('checkoutBtn').disabled = true;
      return;
    }
    el.innerHTML = '';
    data.items.forEach(it => {
      const row = document.createElement('div');
      row.className = 'cart-item';
      row.innerHTML = `<div class="ci-left">${escapeHtml(it.name)}</div>
                       <div class="ci-right">${it.qty} × $${it.price.toFixed(2)} = <strong>$${it.subtotal.toFixed(2)}</strong></div>`;
      el.appendChild(row);
    });
    const total = document.createElement('div');
    total.className = 'cart-total';
    total.innerText = `Total: $${data.total.toFixed(2)}`;
    el.appendChild(total);
    document.getElementById('checkoutBtn').disabled = false;
  } catch (e) {
    showMessage('Could not load cart: ' + e.message, 'error');
    document.getElementById('cart').innerHTML = '<div class="muted">Failed to load cart.</div>';
    document.getElementById('checkoutBtn').disabled = true;
  }
}

document.getElementById('checkoutBtn').addEventListener('click', async () => {
  try {
    const res = await fetch('/api/checkout', { method: 'POST' });
    if (!res.ok) throw new Error('Checkout failed');
    const json = await res.json();
    showMessage(`Checked out. Total: $${json.total.toFixed(2)}`, 'success');
    await refreshCart();
  } catch (e) {
    showMessage(e.message, 'error');
  }
});

document.getElementById('refreshBtn').addEventListener('click', async () => {
  await refreshProducts();
  await refreshCart();
});

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;"}[c]));
}

(async function init() {
  await refreshProducts();
  await refreshCart();
})();
