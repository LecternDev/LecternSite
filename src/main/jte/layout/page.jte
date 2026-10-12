@import tlm.dev.lectern.site.pages.PageFrame
@param PageFrame frame
@param gg.jte.Content content
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="color-scheme" content="light dark">
    <title>${frame.title()} - Lectern for Minecraft Java</title>
    <style>
        :root { --bg: #fbfbfa; --fg: #1b1d1f; --muted: #5d6670; --line: #d9dde1; --accent: #1f5fbf; --code-bg: #eceff2; --warn-bg: #fff1c9; --warn-fg: #4f3a00; --badge-bg: #a24b00; --badge-fg: #ffffff; }
        @media (prefers-color-scheme: dark) {
            :root { --bg: #16181a; --fg: #e6e8ea; --muted: #9aa4ad; --line: #30363b; --accent: #82b4ff; --code-bg: #242a2f; --warn-bg: #3a2e08; --warn-fg: #f3dc9a; --badge-bg: #d07a26; --badge-fg: #111111; }
        }
        * { box-sizing: border-box; }
        body { margin: 0; background: var(--bg); color: var(--fg); font: 16px/1.55 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; }
        .site-header, main, footer { margin: 0 auto; max-width: 56rem; padding-left: 1rem; padding-right: 1rem; }
        .site-header { align-items: baseline; display: flex; flex-wrap: wrap; gap: 0.25rem 0.75rem; padding-top: 1.25rem; }
        .site-name { color: var(--fg); font-size: 1.5rem; font-weight: 700; text-decoration: none; }
        .site-tagline { color: var(--muted); }
        a { color: var(--accent); }
        h1 { font-size: 1.75rem; line-height: 1.2; margin: 1.5rem 0 0.75rem; overflow-wrap: anywhere; }
        h2 { font-size: 1.3rem; margin: 2rem 0 0.25rem; }
        h3 { font-size: 1.05rem; margin: 1.25rem 0 0.25rem; }
        .count, .muted { color: var(--muted); font-weight: 400; }
        .table-wrap { overflow-x: auto; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border-bottom: 1px solid var(--line); padding: 0.45rem 0.5rem; text-align: left; vertical-align: baseline; }
        dl.sheet { display: grid; gap: 0.25rem 1.5rem; grid-template-columns: max-content 1fr; margin: 0; }
        dl.sheet dt { color: var(--muted); }
        dl.sheet dd { margin: 0; overflow-wrap: anywhere; }
        code { background: var(--code-bg); border-radius: 0.25rem; font-size: 0.9em; overflow-wrap: anywhere; padding: 0.05rem 0.3rem; }
        .changes { margin: 0.25rem 0; padding-left: 1.25rem; }
        .changes li { margin: 0.2rem 0; }
        .shifts { color: var(--muted); margin: 0.5rem 0; }
        .unstable { background: var(--badge-bg); border-radius: 0.25rem; color: var(--badge-fg); font-size: 0.75rem; font-weight: 600; margin-left: 0.5rem; padding: 0.1rem 0.4rem; vertical-align: middle; }
        .warning { background: var(--warn-bg); border-radius: 0.25rem; color: var(--warn-fg); margin: 0.5rem 0; padding: 0.5rem 0.75rem; }
        footer { color: var(--muted); font-size: 0.9rem; margin-top: 3rem; padding-bottom: 2rem; }
        footer div { border-top: 1px solid var(--line); padding-top: 1rem; }
        .notice { font-weight: 600; }
        @media (max-width: 36rem) {
            dl.sheet { grid-template-columns: 1fr; }
            dl.sheet dd { margin-bottom: 0.5rem; }
        }
    </style>
</head>
<body>
<header class="site-header">
    <a class="site-name" href="${frame.home()}">Lectern</a>
    <span class="site-tagline">for Minecraft Java</span>
</header>
<main>
    ${content}
</main>
<footer>
    <div>
        <p class="notice">${frame.notice()}</p>
        @if(frame.hasContact())
            <p>Lectern is run by LecternDev. Responsible party and contact: @if(frame.hasContactLink())<a href="${frame.contactHref()}">${frame.contactText()}</a>@else${frame.contactText()}@endif</p>
        @else
            <p>Lectern is run by LecternDev. No contact method is configured: this is a local build and must not be published.</p>
        @endif
    </div>
</footer>
</body>
</html>
