package DriveEase;

/**
 * Single source of truth for all application CSS.
 */
public class StyleSheet {

    public static String get() {
        return """
            .root { -fx-background-color: #030510; }
            .text-field, .password-field {
                -fx-background-color: #090e20;
                -fx-text-fill: #e2e8f0; -fx-prompt-text-fill: #3a4a6a;
                -fx-background-radius: 10; -fx-border-radius: 10;
                -fx-border-color: #1c2c54; -fx-border-width: 1;
                -fx-padding: 10 14; -fx-font-size: 13px;
            }
            .text-field:focused, .password-field:focused {
                -fx-border-color: #38bdff;
                -fx-effect: dropshadow(gaussian, rgba(56,189,255,0.35), 10, 0, 0, 0);
            }
            .combo-box {
                -fx-background-color: #090e20; -fx-border-color: #1c2c54;
                -fx-border-radius: 10; -fx-background-radius: 10;
            }
            .combo-box .list-cell { -fx-text-fill: #e2e8f0; -fx-background-color: #090e20; }
            .combo-box-popup .list-view  { -fx-background-color: #0d1428; -fx-border-color: #1c2c54; }
            .combo-box-popup .list-cell  { -fx-text-fill: #e2e8f0; -fx-background-color: #0d1428; }
            .combo-box-popup .list-cell:hover { -fx-background-color: #162040; }
            .table-view {
                -fx-background-color: #070c1a;
                -fx-table-cell-border-color: transparent; -fx-background-radius: 12;
            }
            .table-view .column-header-background {
                -fx-background-color: #0c1428; -fx-background-radius: 12 12 0 0;
            }
            .table-view .column-header, .table-view .filler {
                -fx-background-color: transparent;
                -fx-border-color: #182848; -fx-border-width: 0 0 1 0;
            }
            .table-row-cell          { -fx-background-color: #070c1a; }
            .table-row-cell:odd      { -fx-background-color: #0a1122; }
            .table-row-cell:selected { -fx-background-color: rgba(56,189,255,0.12); }
            .table-row-cell .text    { -fx-fill: #e2e8f0; }
            .table-cell              { -fx-text-fill: #e2e8f0; -fx-font-size: 12px; -fx-padding: 8 10; }
            .column-header .label    {
                -fx-text-fill: #38bdff; -fx-font-weight: bold;
                -fx-font-size: 11px; -fx-letter-spacing: 0.05em;
            }
            .scroll-pane { -fx-background-color: transparent; }
            .scroll-pane > .viewport { -fx-background-color: transparent; }
            .scroll-bar { -fx-background-color: transparent; }
            .scroll-bar .track { -fx-background-color: #0c1428; -fx-background-radius: 4; }
            .scroll-bar .thumb { -fx-background-color: #1e3060; -fx-background-radius: 4; }
            .tab-pane .tab-header-background { -fx-background-color: #070c1a; }
            .tab-pane .tab           { -fx-background-color: #0c1428; -fx-padding: 8 18; }
            .tab-pane .tab:selected  { -fx-background-color: #0f1a38; }
            .tab-pane .tab .tab-label         { -fx-text-fill: #64748b; -fx-font-size: 13px; }
            .tab-pane .tab:selected .tab-label { -fx-text-fill: #f8fafc; -fx-font-weight: bold; }
            .spinner .increment-arrow-button,
            .spinner .decrement-arrow-button  { -fx-background-color: #162040; -fx-border-color: #1c2c54; }
            .spinner .text-field { -fx-padding: 8 10; }
            .date-picker            { -fx-background-color: #090e20; }
            .date-picker .text-field { -fx-background-color: #090e20; }
            .check-box .box {
                -fx-background-color: #0c1428; -fx-border-color: #1c2c54; -fx-border-radius: 4;
            }
            .check-box:selected .box { -fx-background-color: #38bdff; -fx-border-color: #38bdff; }
            .check-box .label        { -fx-text-fill: #94a3b8; }
            .separator .line         { -fx-border-color: #0f1e3c; -fx-border-width: 1 0 0 0; }
        """;
    }
}

