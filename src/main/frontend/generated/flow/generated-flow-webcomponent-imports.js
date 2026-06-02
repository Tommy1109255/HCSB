import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/combo-box/theme/lumo/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/tabs/theme/lumo/vaadin-tab.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import '@vaadin/integer-field/theme/lumo/vaadin-integer-field.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/multi-select-combo-box/theme/lumo/vaadin-multi-select-combo-box.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/number-field/theme/lumo/vaadin-number-field.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/date-picker/theme/lumo/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/text-area/theme/lumo/vaadin-text-area.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/tabs/theme/lumo/vaadin-tabs.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/time-picker/theme/lumo/vaadin-time-picker.js';
import 'Frontend/generated/jar-resources/vaadin-time-picker/timepickerConnector.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'a6601e48f63c350c4c003ebb05cf06f34445e2c96065d9208d9faf50bba0116d') {
    pending.push(import('./chunks/chunk-0c9b4fc89601d24f25720ab5642612139ca5f78df5d0fe770f8570e13e83adb5.js'));
  }
  if (key === 'fa09b61c3e8cc32e77d13bd8633f35dd64185ec66d78389eefaed42aa4fe14b3') {
    pending.push(import('./chunks/chunk-0c9b4fc89601d24f25720ab5642612139ca5f78df5d0fe770f8570e13e83adb5.js'));
  }
  if (key === '91594b1ee2a848380775a4fef978d8145517e07904c03d0df3635c050edb7e71') {
    pending.push(import('./chunks/chunk-0c9b4fc89601d24f25720ab5642612139ca5f78df5d0fe770f8570e13e83adb5.js'));
  }
  if (key === 'f062372f086e023f0248f287226ba819b1745929671c64da684fa1a3e36d9c10') {
    pending.push(import('./chunks/chunk-0c9b4fc89601d24f25720ab5642612139ca5f78df5d0fe770f8570e13e83adb5.js'));
  }
  if (key === '54e1d8bcb5df737b7afbee74ee0602d97d46d6be5f10ae6cefce2dad30e868b5') {
    pending.push(import('./chunks/chunk-0c9b4fc89601d24f25720ab5642612139ca5f78df5d0fe770f8570e13e83adb5.js'));
  }
  if (key === '1c6832354bac66e70361a8fe929159316e9dcd23a9a8f279a14c9e3d165aee94') {
    pending.push(import('./chunks/chunk-5813c1fe59bf6574b2affc4a6fb595a78f47d30b2ecd74892efb0fcca3e45a33.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}