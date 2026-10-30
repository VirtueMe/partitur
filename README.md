# Partitur

A tree-structured, gated agent swarm: it turns one brief into a tree of isolated sub-projects, works them in parallel, and sends what it cannot resolve upward until someone with the mandate decides.

The full specification is [docs/Brief.md](docs/Brief.md). Role templates and skills live in their own catalogue, [VirtueMe/repertoire](https://github.com/VirtueMe/repertoire).

## Status

Being built by [conductor-swarm-forge](https://github.com/VirtueMe/conductor-swarm-forge) from the brief. Nothing here is implemented yet; the first deliverable is the architect's plan and the first ADR.

## Development

```
bb test
```

Babashka for runtime, EDN for events, htmx for the UI. Packages and layout are decided by the architect and recorded in `docs/adr/`.

## Licence

MIT.
