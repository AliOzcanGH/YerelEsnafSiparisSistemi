// API Base URL
const API_BASE = '/api';

// Sepet verilerini localStorage'dan al
let cart = JSON.parse(localStorage.getItem('cart')) || [];

// Kupon bilgilerini sakla
let appliedCoupon = JSON.parse(localStorage.getItem('appliedCoupon')) || null;

// Tüm ürünleri sakla (filtreleme için)
let allProducts = [];
let allCategories = [];
let currentFilters = {
    search: '',
    category: null,
    minPrice: null,
    maxPrice: null,
    inStock: false
};

// Sayfa yüklendiğinde
document.addEventListener('DOMContentLoaded', function() {
    // Kategorileri yükle
    if (document.getElementById('categoryFilters')) {
        loadCategories();
    }
    
    // Arama kutusu event listener
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            currentFilters.search = e.target.value;
            loadProductsWithFilters();
        });
    }
    
    // Fiyat filtresi
    const applyPriceBtn = document.getElementById('applyPriceFilter');
    if (applyPriceBtn) {
        applyPriceBtn.addEventListener('click', () => {
            currentFilters.minPrice = document.getElementById('minPrice').value || null;
            currentFilters.maxPrice = document.getElementById('maxPrice').value || null;
            loadProductsWithFilters();
        });
    }
    
    // Stok filtresi
    const stockFilter = document.getElementById('stockFilter');
    if (stockFilter) {
        stockFilter.addEventListener('change', (e) => {
            currentFilters.inStock = e.target.checked;
            loadProductsWithFilters();
        });
    }

    // Eğer API'den ürün yüklenen grid varsa onu doldur
    const productGridEl = document.getElementById('productGrid');
    if (productGridEl) {
        // İlk yükleme - API'den getir
        loadProductsWithFilters();
    }

    // Thymeleaf ile render edilen ürün kartlarındaki butonlara event bağla (event delegation)
    document.body.addEventListener('click', function(e) {
        const btn = e.target.closest('button.add-to-cart');
        if (!btn) return;
        const id = Number(btn.dataset.id);
        const name = btn.dataset.name;
        const price = Number(btn.dataset.price);
        if (!btn.disabled) {
            addToCart(id, name, price);
        }
    });

    updateCartCount();
});

// Kategorileri yükle
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE}/categories`);
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ error: 'Bilinmeyen hata' }));
            console.error('Kategoriler yüklenirken hata:', errorData.error || `HTTP ${response.status}`);
            return;
        }
        
        const data = await response.json();
        
        // Eğer hata mesajı varsa
        if (data && data.error) {
            console.error('Kategoriler yüklenirken hata:', data.error);
            return;
        }
        
        // Array kontrolü
        if (!Array.isArray(data)) {
            console.error('Beklenmeyen yanıt formatı:', data);
            allCategories = [];
            return;
        }
        
        allCategories = data;
        
        const container = document.getElementById('categoryFilters');
        if (!container) return;
        
        // "Tümü" butonunu koru
        const allBtn = container.querySelector('[data-category="all"]');
        
        // Kategori butonlarını ekle
        allCategories.forEach(cat => {
            const btn = document.createElement('button');
            btn.className = 'category-btn';
            btn.dataset.category = cat.id;
            btn.innerHTML = `${cat.icon || '📦'} ${cat.name}`;
            btn.addEventListener('click', () => {
                // Aktif sınıfı değiştir
                container.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                
                // Filtreyi uygula
                currentFilters.category = cat.id;
                loadProductsWithFilters();
                
                // Başlığı güncelle
                document.getElementById('productTitle').textContent = cat.name;
            });
            container.appendChild(btn);
        });
        
        // "Tümü" butonu event'i
        if (allBtn) {
            allBtn.addEventListener('click', () => {
                container.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
                allBtn.classList.add('active');
                currentFilters.category = null;
                loadProductsWithFilters();
                document.getElementById('productTitle').textContent = 'Tüm Ürünler';
            });
        }
    } catch (error) {
        console.error('Kategoriler yüklenirken hata:', error);
    }
}

// Filtrelerle ürünleri yükle
async function loadProductsWithFilters() {
    try {
        // Query parametrelerini oluştur
        const params = new URLSearchParams();
        if (currentFilters.search) params.append('search', currentFilters.search);
        if (currentFilters.category) params.append('categoryId', currentFilters.category);
        if (currentFilters.minPrice) params.append('minPrice', currentFilters.minPrice);
        if (currentFilters.maxPrice) params.append('maxPrice', currentFilters.maxPrice);
        
        const url = `${API_BASE}/products${params.toString() ? '?' + params.toString() : ''}`;
        const response = await fetch(url);
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ error: 'Bilinmeyen hata' }));
            throw new Error(errorData.error || `HTTP ${response.status}`);
        }
        
        let products = await response.json();
        
        // Eğer hata mesajı varsa
        if (products && products.error) {
            throw new Error(products.error);
        }
        
        // Array kontrolü
        if (!Array.isArray(products)) {
            console.error('Beklenmeyen yanıt formatı:', products);
            products = [];
        }
        
        // Stok filtresi client-side
        if (currentFilters.inStock) {
            products = products.filter(p => p.stock > 0);
        }
        
        allProducts = products;
        displayProducts(products);
        
        // Ürün sayısını göster
        const countEl = document.getElementById('productCount');
        if (countEl) {
            countEl.textContent = `${products.length} ürün bulundu`;
        }
    } catch (error) {
        console.error('Ürünler yüklenirken hata:', error);
        const grid = document.getElementById('productGrid');
        if (grid) {
            grid.innerHTML = `<div class="loading">Ürünler yüklenirken hata oluştu: ${error.message}</div>`;
        }
    }
}

// Ürünleri API'den yükle
async function loadProducts() {
    try {
        const response = await fetch(`${API_BASE}/products`);
        const products = await response.json();
        
        allProducts = products; // Tüm ürünleri sakla
        applyFilters(); // Filtreleri uygula ve göster
    } catch (error) {
        console.error('Ürünler yüklenirken hata:', error);
        const grid = document.getElementById('productGrid');
        if (grid) {
            grid.innerHTML = '<div class="loading">Ürünler yüklenirken hata oluştu.</div>';
        }
    }
}

// Filtreleri uygula (API'den gelen ürünler için)
function applyFilters() {
    const stockFilter = document.getElementById('stockFilter');
    const onlyInStock = stockFilter ? stockFilter.checked : false;
    
    let filteredProducts = allProducts;
    
    // Stok filtresi
    if (onlyInStock) {
        filteredProducts = filteredProducts.filter(p => p.stock > 0);
    }
    
    displayProducts(filteredProducts);
}

// Filtreleri uygula (Thymeleaf ile render edilen ürünler için)
function applyFiltersForThymeleaf() {
    const stockFilter = document.getElementById('stockFilter');
    const onlyInStock = stockFilter ? stockFilter.checked : false;
    const productGrid = document.getElementById('productGrid');
    
    if (!productGrid) return;
    
    // Tüm ürün kartlarını göster/gizle
    const productCards = productGrid.querySelectorAll('.product-card');
    productCards.forEach(card => {
        const btn = card.querySelector('.add-to-cart');
        if (!btn) return;
        
        const stock = Number(btn.dataset.stock);
        
        if (onlyInStock && stock === 0) {
            card.style.display = 'none';
        } else {
            card.style.display = 'block';
        }
    });
}

// Ürünleri sayfada göster
function displayProducts(products) {
    const productGrid = document.getElementById('productGrid');
    if (!productGrid) return;
    
    if (products.length === 0) {
        productGrid.innerHTML = '<div class="loading">Henüz ürün bulunmuyor.</div>';
        return;
    }
    
    productGrid.innerHTML = products.map(product => {
        const hasImage = product.imageUrl && product.imageUrl.trim() !== '';
        const rating = product.averageRating || 0;
        const reviewCount = product.reviewCount || 0;
        const stars = '⭐'.repeat(Math.round(rating)) + '☆'.repeat(5 - Math.round(rating));
        
        return `
        <div class="product-card">
            ${hasImage 
                ? `<div class="product-image"><img src="${product.imageUrl}" alt="${product.name}" /></div>`
                : `<div class="product-image-placeholder"><span>📦</span></div>`
            }
            <h4>${product.name}</h4>
            ${reviewCount > 0 ? `
                <div style="color: #ffa500; font-size: 0.9rem; margin: 0.3rem 0;">
                    ${stars} <span style="color: #666;">(${reviewCount})</span>
                </div>
            ` : ''}
            <div class="price">₺${product.price.toFixed(2)}</div>
            <div class="description">${product.description || 'Açıklama yok'}</div>
            <div class="stock">Stok: ${product.stock} adet</div>
            <button class="add-to-cart" data-id="${product.id}" data-name="${product.name}" data-price="${product.price}" data-stock="${product.stock}" ${product.stock === 0 ? 'disabled' : ''} style="width: 100%; margin-top: 0.5rem;">
                ${product.stock === 0 ? 'Stokta Yok' : 'Sepete Ekle'}
            </button>
        </div>
        `;
    }).join('');
}

// Sepete ürün ekle
function addToCart(productId, productName, price) {
    const existingItem = cart.find(item => item.id === productId);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            id: productId,
            name: productName,
            price: price,
            quantity: 1
        });
    }
    
    // Sepeti localStorage'a kaydet
    localStorage.setItem('cart', JSON.stringify(cart));
    
    // Sepet sayacını güncelle
    updateCartCount();
    
    // Başarı mesajı göster
    showMessage(`${productName} sepete eklendi!`, 'success');
}

// Sepet sayacını güncelle
function updateCartCount() {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    // Hem static hem thymeleaf rotaları için dene
    let cartLink = document.querySelector('a[href="/cart"]');
    if (!cartLink) cartLink = document.querySelector('a[href="cart.html"]');
    if (cartLink) {
        // Badge'i kaldır ve yeniden ekle
        const existingBadge = cartLink.querySelector('.cart-badge');
        if (existingBadge) {
            existingBadge.remove();
        }
        
        // Sadece "Sepet" text'i bırak
        cartLink.childNodes.forEach(node => {
            if (node.nodeType === 3) { // Text node
                node.textContent = 'Sepet';
            }
        });
        
        // Ürün varsa badge ekle
        if (totalItems > 0) {
            const badge = document.createElement('span');
            badge.className = 'cart-badge';
            badge.textContent = totalItems;
            cartLink.appendChild(badge);
        }
    }
}

// Modern Toast Notification göster
function showMessage(message, type = 'success') {
    // Toast container oluştur
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    // İkon seç
    let icon = '✓';
    let title = 'Başarılı';
    if (type === 'error') {
        icon = '✕';
        title = 'Hata';
    } else if (type === 'warning') {
        icon = '⚠';
        title = 'Uyarı';
    } else if (type === 'info') {
        icon = 'ℹ';
        title = 'Bilgi';
    }
    
    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-message">${message}</div>
        </div>
        <button class="toast-close">×</button>
    `;
    
    document.body.appendChild(toast);
    
    // Kapatma butonu
    const closeBtn = toast.querySelector('.toast-close');
    closeBtn.addEventListener('click', () => {
        toast.remove();
    });
    
    // 3 saniye sonra otomatik kapat
    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 3000);
}

// --- Sepet Sayfası Yardımcıları ---
function renderCart() {
    const container = document.getElementById('cartContainer');
    const summary = document.getElementById('cartSummary');
    const empty = document.getElementById('emptyCart');
    if (!container) return;

    // localStorage'dan kupon bilgisini yükle
    const savedCoupon = localStorage.getItem('appliedCoupon');
    if (savedCoupon) {
        try {
            appliedCoupon = JSON.parse(savedCoupon);
        } catch (e) {
            appliedCoupon = null;
        }
    }

    if (!cart || cart.length === 0) {
        container.innerHTML = '';
        if (summary) summary.style.display = 'none';
        if (empty) empty.style.display = 'block';
        updateCartCount();
        // Sepet boşsa kuponu temizle
        appliedCoupon = null;
        localStorage.removeItem('appliedCoupon');
        return;
    }

    if (empty) empty.style.display = 'none';

    let subtotal = 0;
    container.innerHTML = cart.map(item => {
        const lineTotal = item.price * item.quantity;
        subtotal += lineTotal;
        return `
        <div class="product-card" data-id="${item.id}">
            <h4>${item.name}</h4>
            <div class="price">Birim: ₺${item.price.toFixed(2)}</div>
            <div class="description">Adet: ${item.quantity} | Tutar: ₺${lineTotal.toFixed(2)}</div>
            <div class="cart-controls">
                <button data-action="dec">-</button>
                <span>${item.quantity}</span>
                <button data-action="inc">+</button>
                <button class="cart-remove" data-action="remove">Kaldır</button>
            </div>
        </div>`;
    }).join('');

    if (summary) {
        summary.style.display = 'block';
        
        // Ara toplam
        const cartSubtotalEl = document.getElementById('cartSubtotal');
        if (cartSubtotalEl) cartSubtotalEl.textContent = `₺${subtotal.toFixed(2)}`;
        
        // Kupon varsa indirimi hesapla
        let discount = 0;
        let total = subtotal;
        if (appliedCoupon && appliedCoupon.valid) {
            discount = appliedCoupon.discount;
            total = subtotal - discount;
            
            // İndirim satırını göster
            const discountRow = document.getElementById('discountRow');
            const discountPercent = document.getElementById('discountPercent');
            const cartDiscount = document.getElementById('cartDiscount');
            if (discountRow) discountRow.style.display = 'flex';
            if (discountPercent) discountPercent.textContent = `${appliedCoupon.percentage || 0}%`;
            if (cartDiscount) cartDiscount.textContent = `-₺${discount.toFixed(2)}`;
        } else {
            // İndirim satırını gizle
            const discountRow = document.getElementById('discountRow');
            if (discountRow) discountRow.style.display = 'none';
        }
        
        // Toplam
        const cartTotalEl = document.getElementById('cartTotal');
        if (cartTotalEl) cartTotalEl.textContent = `₺${total.toFixed(2)}`;
        
        // Kupon input'unu güncelle (eğer kupon varsa)
        const couponInput = document.getElementById('couponInput');
        const couponMessage = document.getElementById('couponMessage');
        if (appliedCoupon && appliedCoupon.valid && couponInput && couponMessage) {
            couponInput.value = appliedCoupon.code;
            couponMessage.innerHTML = `<span style="color: #28a745;">✓ Kupon uygulandı! İndirim: ₺${discount.toFixed(2)}</span>`;
        }
    }
    updateCartCount();
}

function changeQuantity(productId, delta) {
    const idx = cart.findIndex(i => i.id === productId);
    if (idx === -1) return;
    cart[idx].quantity += delta;
    if (cart[idx].quantity <= 0) {
        cart.splice(idx, 1);
    }
    localStorage.setItem('cart', JSON.stringify(cart));
}

function removeFromCart(productId) {
    cart = cart.filter(i => i.id !== productId);
    localStorage.setItem('cart', JSON.stringify(cart));
}

function clearCart() {
    cart = [];
    appliedCoupon = null;
    localStorage.setItem('cart', JSON.stringify(cart));
    localStorage.removeItem('appliedCoupon');
}

// Kupon kodunu uygula
async function applyCoupon() {
    const couponInput = document.getElementById('couponInput');
    const couponMessage = document.getElementById('couponMessage');
    
    if (!couponInput || !couponMessage) return;
    
    const code = couponInput.value.trim();
    if (!code) {
        couponMessage.innerHTML = '<span style="color: #dc3545;">Lütfen bir kupon kodu girin.</span>';
        return;
    }
    
    // Sepet toplamını hesapla
    let subtotal = 0;
    cart.forEach(item => {
        subtotal += item.price * item.quantity;
    });
    
    if (subtotal === 0) {
        couponMessage.innerHTML = '<span style="color: #dc3545;">Sepetiniz boş!</span>';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/coupons/validate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                code: code,
                orderAmount: subtotal
            })
        });
        
        if (!response.ok) {
            // HTTP hatası durumunda
            const errorData = await response.json().catch(() => ({ error: 'Bilinmeyen hata oluştu' }));
            couponMessage.innerHTML = `<span style="color: #dc3545;">${errorData.error || 'Kupon uygulanırken hata oluştu'}</span>`;
            appliedCoupon = null;
            localStorage.removeItem('appliedCoupon');
            showMessage(errorData.error || 'Kupon uygulanırken hata oluştu!', 'error');
            return;
        }
        
        const result = await response.json();
        
        if (result.error) {
            couponMessage.innerHTML = `<span style="color: #dc3545;">${result.error}</span>`;
            appliedCoupon = null;
            localStorage.removeItem('appliedCoupon');
            showMessage(result.error, 'error');
        } else if (result.valid) {
            appliedCoupon = {
                code: code,
                valid: true,
                discount: result.discount,
                percentage: result.discountPercentage || 0,
                description: result.couponDescription || ''
            };
            localStorage.setItem('appliedCoupon', JSON.stringify(appliedCoupon));
            couponMessage.innerHTML = `<span style="color: #28a745;">✓ Kupon uygulandı! İndirim: ₺${result.discount.toFixed(2)}</span>`;
            showMessage('Kupon başarıyla uygulandı!', 'success');
            
            // Sepeti yeniden render et (indirimli toplam için)
            renderCart();
        }
    } catch (error) {
        console.error('Kupon uygulama hatası:', error);
        couponMessage.innerHTML = '<span style="color: #dc3545;">Kupon uygulanırken hata oluştu: ' + error.message + '</span>';
        showMessage('Kupon uygulanırken hata oluştu: ' + error.message, 'error');
        appliedCoupon = null;
        localStorage.removeItem('appliedCoupon');
    }
}

// Checkout işlemi
async function processCheckout() {
    if (cart.length === 0) {
        showMessage('Sepetiniz boş!', 'error');
        return;
    }
    
    try {
        const checkoutBtn = document.getElementById('checkoutBtn');
        if (checkoutBtn) {
            checkoutBtn.disabled = true;
            checkoutBtn.textContent = 'İşleniyor...';
        }
        
        // Kupon bilgisini checkout'a ekle
        const checkoutData = {
            items: cart,
            couponCode: appliedCoupon ? appliedCoupon.code : null
        };
        
        const response = await fetch(`${API_BASE}/checkout`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(checkoutData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage(result.message || 'Siparişiniz başarıyla oluşturuldu!', 'success');
            clearCart();
            setTimeout(() => {
                window.location.href = '/';
            }, 2000);
        } else {
            showMessage(result.message || 'Sipariş oluşturulurken hata oluştu!', 'error');
        }
    } catch (error) {
        console.error('Checkout hatası:', error);
        showMessage('Sipariş oluşturulurken hata oluştu: ' + error.message, 'error');
    } finally {
        const checkoutBtn = document.getElementById('checkoutBtn');
        if (checkoutBtn) {
            checkoutBtn.disabled = false;
            checkoutBtn.textContent = 'Siparişi Ver';
        }
    }
}

