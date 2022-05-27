$(document).ready( function () {
    var t = $('#jobsTable').DataTable({
        "order": [ 2, 'desc' ]
    });

    t.on( 'order.dt search.dt', function () {
        t.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
            cell.innerHTML = i+1;
        } );
    } ).draw();
} );

