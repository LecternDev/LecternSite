@import tlm.dev.lectern.site.pages.VersionListView
@import tlm.dev.lectern.site.pages.VersionRow
@param VersionListView view
@template.layout.page(frame = view.frame(), content = @`
    <h1>Versions</h1>
    <p>Every version tracked by Lectern, newest first. Snapshots, pre-releases and release candidates are marked as unstable.</p>
    @if(view.rows().isEmpty())
        <p>No versions have been published yet.</p>
    @else
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th scope="col">Version</th>
                        <th scope="col">Kind</th>
                        <th scope="col">Released</th>
                    </tr>
                </thead>
                <tbody>
                @for(VersionRow row : view.rows())
                    <tr data-version="${row.id()}">
                        <th scope="row"><a href="${row.href()}">${row.id()}</a>@if(row.unstable()) <span class="unstable">unstable</span>@endif</th>
                        <td>${row.kind()}</td>
                        <td><time datetime="${row.releasedAt()}">${row.releasedOn()}</time></td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    @endif
`)
