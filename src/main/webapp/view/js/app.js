/* =========================================================================
   FUNCIÓN PRINCIPAL DE ARRANQUE
   ========================================================================= */
async function inicializarPagina() {
    await cargarNoticias();
    comprobarSesion();
}

/* =========================================================================
   EVENTOS (LISTENERS)
   ========================================================================= */
document.addEventListener('DOMContentLoaded', () => {
    inicializarPagina();

    document.getElementById('btn-abrir-login').onclick = () => abrirModal('modal-login');
    document.getElementById('btn-cerrar-login').onclick = () => cerrarModal('modal-login');
    document.getElementById('form-login').onsubmit = iniciarSesion;
});

/* =========================================================================
   PETICIONES AL BACKEND (SERVLETS JAVA) Y PINTADO DEL DOM
   ========================================================================= */
async function cargarNoticias() {
    try {
        const res = await fetch('../api/noticias/listar');
        const noticias = await res.json();
        const grid = document.getElementById('grid-todas');

        if (!grid) return;

        grid.innerHTML = '';

        noticias.forEach(n => {
            const art = document.createElement('article');
            art.className = 'tarjeta-noticia';

            const imgSrc = (n.nombreImagen && n.nombreImagen !== 'null' && n.nombreImagen !== '')
                ? `../uploads/${n.nombreImagen}`
                : 'https://placehold.co/600x400/1a1a2e/e0e0e0?text=Nexo+Digital';

            art.innerHTML = `
                <img src="${imgSrc}" class="imagen-placeholder" alt="${n.titulo}">
                <div class="contenido-tarjeta">
                    <h3>${n.titulo}</h3>
                </div>
            `;

            art.onclick = () => window.location.href = 'noticia.html?id=' + n.id;
            grid.appendChild(art);
        });
    } catch (e) {
        console.error("Error al cargar noticias:", e);
    }
}

function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function cerrarModal(id) { document.getElementById(id).style.display = 'none'; }

/* =========================================================================
   SISTEMA DE SESIONES Y LOGIN
   ========================================================================= */
async function iniciarSesion(e) {
    e.preventDefault();

    const usuario = document.getElementById('usuario').value;
    const password = document.getElementById('password').value;

    try {
        const res = await fetch('../api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `usuario=${encodeURIComponent(usuario)}&password=${encodeURIComponent(password)}`
        });

        if (res.ok) {
            window.location.href = 'admin.html';
        } else {
            alert('Usuario o contraseña incorrectos');
        }
    } catch (err) {
        console.error('Error al iniciar sesión:', err);
        alert('Error de conexión con el servidor');
    }
}

async function comprobarSesion() {
    try {
        const res = await fetch('../api/sesion');
        const data = await res.json();
        actualizarUI(data.logueado);
    } catch (err) {
        console.error('Error al comprobar sesión:', err);
        actualizarUI(false);
    }
}

function actualizarUI(logueado) {
    const controlesAdmin = document.getElementById('controles-admin');
    const controlesPublicos = document.getElementById('controles-publicos');

    if (logueado) {
        controlesAdmin.style.display = 'block';
        controlesPublicos.style.display = 'none';
    } else {
        controlesAdmin.style.display = 'none';
        controlesPublicos.style.display = 'block';
    }
}

/* =========================================================================
   REAJUSTE AUTOMÁTICO AL CAMBIAR DE PESTAÑA (REFLOW BUG FIX)
   ========================================================================= */
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
        window.dispatchEvent(new Event('resize'));
    }
});

window.addEventListener('pageshow', () => {
    window.dispatchEvent(new Event('resize'));
});