@import tlm.dev.lectern.site.pages.VersionRow
@param java.util.List<VersionRow> rows
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Minecraft Java versions - Lectern</title>
    <style>
        body { font-family: system-ui, sans-serif; margin: 2rem auto; max-width: 48rem; padding: 0 1rem; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border-bottom: 1px solid #d0d0d0; padding: 0.4rem 0.6rem; text-align: left; }
        .unstable { background: #b45309; border-radius: 0.25rem; color: #ffffff; font-size: 0.75rem; font-weight: 600; margin-left: 0.5rem; padding: 0.1rem 0.4rem; }
    </style>
</head>
<body>
<main>
    <h1>Minecraft Java versions</h1>
    <p>Every version tracked by Lectern, newest first. Snapshots, pre-releases and release candidates are marked as unstable.</p>
    <table>
        <thead>
            <tr>
                <th scope="col">Version</th>
                <th scope="col">Kind</th>
                <th scope="col">Released</th>
            </tr>
        </thead>
        <tbody>
        @for(VersionRow row : rows)
            <tr data-version="${row.id()}">
                <th scope="row">${row.id()}@if(row.unstable()) <span class="unstable">unstable</span>@endif</th>
                <td>${row.kind()}</td>
                <td><time datetime="${row.releasedAt()}">${row.releasedOn()}</time></td>
            </tr>
        @endfor
        </tbody>
    </table>
</main>
</body>
</html>
