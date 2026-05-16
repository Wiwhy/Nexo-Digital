window.alert = function(message) {
    const overlay = document.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.top = '0';
    overlay.style.left = '0';
    overlay.style.width = '100%';
    overlay.style.height = '100%';
    overlay.style.backgroundColor = 'rgba(0,0,0,0.5)';
    overlay.style.display = 'flex';
    overlay.style.alignItems = 'center';
    overlay.style.justifyContent = 'center';
    overlay.style.zIndex = '999999';
    overlay.style.backdropFilter = 'blur(4px)';

    const modal = document.createElement('div');
    modal.className = 'tarjeta-admin'; // Reutilizamos estilos
    modal.style.width = 'auto';
    modal.style.minWidth = '300px';
    modal.style.maxWidth = '90%';
    modal.style.padding = '2rem';
    modal.style.textAlign = 'center';

    const title = document.createElement('h3');
    title.textContent = 'Nexo Digital dice:';
    title.style.margin = '0 0 1rem 0';
    title.style.color = 'var(--texto-principal)';
    title.style.fontSize = '1.3rem';

    const text = document.createElement('p');
    text.textContent = message;
    text.style.margin = '0 0 1.5rem 0';
    text.style.color = 'var(--texto-secundario)';
    text.style.fontSize = '1rem';

    const btn = document.createElement('button');
    btn.textContent = 'Aceptar';
    btn.className = 'btn-azul btn-pequeno';
    btn.style.margin = '0 auto';
    btn.style.display = 'block';

    btn.onclick = () => document.body.removeChild(overlay);

    modal.appendChild(title);
    modal.appendChild(text);
    modal.appendChild(btn);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
};
