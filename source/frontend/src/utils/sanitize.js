const THE_DUOC_PHEP = new Set(['P', 'BR', 'STRONG', 'EM', 'UL', 'OL', 'LI', 'H2', 'H3', 'H4', 'A']);
const THE_NGUY_HIEM = new Set(['SCRIPT', 'STYLE', 'IFRAME', 'OBJECT', 'EMBED', 'FORM']);

/**
 * Chỉ giữ lại một tập HTML trình bày tối thiểu cho mô tả sản phẩm.
 * Mọi script, style và thuộc tính sự kiện đều bị loại bỏ trước khi render.
 */
export function lamSachHtml(html) {
  if (!html || typeof document === 'undefined') return '';

  const template = document.createElement('template');
  template.innerHTML = html;

  [...template.content.querySelectorAll('*')].forEach((element) => {
    if (!THE_DUOC_PHEP.has(element.tagName)) {
      if (THE_NGUY_HIEM.has(element.tagName)) element.remove();
      else element.replaceWith(...element.childNodes);
      return;
    }

    const href = element.tagName === 'A' ? element.getAttribute('href') : null;
    [...element.attributes].forEach((attribute) => element.removeAttribute(attribute.name));
    if (element.tagName === 'A') {
      if (href && /^(https?:\/\/|\/(?![\/\\])|#)/i.test(href)) {
        element.setAttribute('href', href);
        element.setAttribute('rel', 'noopener noreferrer');
      }
    }
  });

  return template.innerHTML;
}
