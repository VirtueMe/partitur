# Brief: Build the gated tree swarm

Oct 28, 2026 · @Benny Molowny-Thomas

## Purpose

The current swarm builds its successor, Partitur: a tree-structured, gated swarm in a new repository, VirtueMe/partitur, with its catalogue of role templates and skills in VirtueMe/repertoire, and the human acting only at named gates.

This brief is addressed to the current swarm. It has two parts that must not be mixed:

- **Part A, What to build**, describes the product. Its tree, states, escalations and gates are features of the new swarm.
- **Part B, How to build it**, governs the current swarm while it works. Its gates and rules apply to the build, not to the product.

When a rule appears in both parts, the one in Part B binds the builder and the one in Part A is something to implement.

## Starting state

The architect plans from what already exists, not from scratch.

- **Repertoire is established.** VirtueMe/repertoire is public under MIT and released as v0.1.0, seeded by the human. It holds the role schema, the six starting roles (architect, coder, tester, reviewer, merger, writer), the starting skills (worktree, commit, review) and a `bb check` that runs in CI. Release Please is configured as a single package. Seeding is done and is not part of the build.
- **Repertoire's roles are already versioned by folder.** Each role has a `1.0.0/` folder with `role.edn` and `instructions.md`, and the role root holds the latest version for compatibility. The architect checks the repository for what remains of this brief's catalogue model, such as versioned skills, team presets, the immutability check and certification, and plans the rest as pull requests to Repertoire that only the human merges.
- **Partitur** is the new repository for the build, VirtueMe/partitur.
- **The current swarm** works locally with file-based issues and logging, and has no access to GitHub, as described in Part B.

The architect's first deliverable is a plan: the first ADR, the issue tree for phase 1, and where the Repertoire migration fits. It is presented at the first gate before any code is written.

## Part A: Outcome

A swarm, in its own repository, that turns one brief into a tree of isolated sub-projects, works them in parallel, and sends what it cannot resolve upward until someone with the mandate decides.

Done means all of the following hold:

1. Given a brief, the architect assesses it and either keeps it as one project or decomposes it into sub-projects, each with its own sub-brief, issue and worktree.
2. Contracts between sibling branches are written and locked before any of those branches start.
3. A coder that hits something outside its sub-brief escalates instead of deciding, and the escalation reaches the level that owns the decision.
4. Questions that reach the human arrive in the decision panel with options, consequences and a recommendation, and the answer travels back down through every conductor to the one that asked.
5. A flat project runs unchanged as a tree of depth one, so the new swarm can do everything the current one does.
6. Stretch: the new swarm can take a brief for its own next version and build it, with the human only at the gates.

## Part A: Design

Every node in the tree is a project with an issue, a sub-brief, a conductor and a kanban board. A parent owns the contracts between its children and the integration of their work.

Issues follow the tree. The initial task is the root parent issue. When a branch is created, it gets its own parent issue, which is a sub-issue of the issue it was given to work on, and its tasks are sub-issues of that. Each branch therefore has its own issue list, and the whole tree can be read from any issue upward.

| Role | Decides | Never does |
| --- | --- | --- |
| Human | Anything escalated past the architect, merges to main, new or changed roles and skills | Relay context between sessions |
| Architect | Decomposition, contracts, revisions after an escalation | Write implementation code |
| Conductor | Routing: every escalation passes through it on the way up, and every answer on the way down | Make design decisions |
| Coder | How to implement within its sub-brief and contracts | Change a contract or decide something another task depends on |

The table shows the structural roles, human and conductor, which exist in every project, alongside architect and coder, the most common templates. All other roles come from role templates, chosen per brief, and each role is configured with any agent or model. The tree does not assume a vendor.

### Role templates

Architect and coder are no longer hard-coded. A role template describes one role, and the architect picks a team of templates for each brief and sub-brief during assessment.

Each template defines:

- **Responsibility:** what the role produces and the gate it owns
- **Decides / never does:** its mandate, and what it must escalate
- **Inputs and outputs:** which issues, contracts, branches and artifacts it reads and writes
- **Default agent or model:** overridable per project

Starting catalogue:

| Template | Produces | Owns the gate |
| --- | --- | --- |
| Architect | Decomposition, contracts, ADRs | Contract lock |
| Coder | Implementation within one sub-brief | None; hands to tester |
| Tester | Tests against the contract and acceptance criteria, written independently of the coder | Tests pass |
| Reviewer | Review for simplicity, correctness and fit with ADRs | Review approved |
| Merger | Integration of child branches into the parent branch, resolving conflicts within contracts | Parent branch green |
| Writer | Docs and ADR text kept in step with the code | Docs current |

Rules for templates:

- Separation of duties: the tester and reviewer are never the same agent instance as the coder whose work they check.
- The merger integrates only into the parent branch. Merging to main stays a human gate.
- A brief may use any subset; a docs-only brief might need only writer and reviewer.
- A missing role or skill is an escalation like any other. Any agent that finds the team can't solve its task raises a proposal: what it is for, and why the existing roles and skills fall short. It travels up the conductor tree and becomes a pull request on the catalogue, which only the human can merge.

### Where templates and skills live

Role templates and skills live in a catalogue repository of their own, separate from any project. Git provides what approval needs:

- **A proposal is a pull request.** When an escalation for a new or changed role or skill is accepted for consideration, the asking conductor opens a PR against the catalogue with the change and the reasoning from the escalation.
- **Approval is the merge.** Branch protection lets only the human merge. The decision panel links to the PR, so answering the escalation and approving the change are the same act.
- **Pinning is the commit.** Repertoire is released with Release Please, and each project's team.edn pins a catalogue release tag together with the exact version of every role and skill it uses. Merging a change approves it; merging the release pull request publishes it. A merged change reaches a project only when a PR in that project moves the pin, and that PR is also merged by the human.
- **No mid-run changes.** A running swarm keeps the catalogue version it started with, so an approved change never alters behaviour halfway through a build.

The swarm has write access to open PRs on the catalogue and no permission to merge them, so the invariant is enforced by the repository, not by the agents' instructions.

### Execution environment

Every agent runs in a container, and what it may write is decided by its mounts, not by its instructions.

- The catalogue is checked out once per approved commit into a shared location and mounted read-only into every container that needs it. Worktrees and projects on the same version share one checkout.
- Each role gets the mounts its template needs: a coder writes only its own worktree, a reviewer reads everything and writes nothing but its review, a merger writes the parent branch.
- Credentials follow the same rule: only the roles that open PRs get a token that can, and no role gets permission to merge to main or into the catalogue.
- A CI check stays as the backstop: it fails any branch where the catalogue pin moves outside a dedicated update PR.

Sessions stay on the host:

- tmux runs on the host, and each pane starts its agent with `docker exec -it <container> …`. Keystrokes and pane captures pass through the exec, so the swarm keeps a single view and the tmux bridge works unchanged.
- Access to the host's tmux socket is a capability granted per role, like mounts and tokens. The conductor gets it, because routing between panes is its job. No other role does, so no agent can type into another agent's session.
- No container mounts the tmux socket unless its role template grants it.

To verify early: that Claude Code, Codex and pi all read skills from a mounted directory outside the worktree.

### Event log and issue trackers

The swarm's source of truth is an append-only event log, not an issue tracker. Every operation is recorded as an event before anything else happens: creating an issue, commenting, changing a state, escalating, recording a decision, proposing a role or skill.

- **The UI reads projections of the log.** While the swarm works, the tree view, each branch's own issue list and kanban, and the decision panel are built from the log. Following progress on GitHub is not needed.
- **Issue trackers are projections too.** GitHub and Gitea are backends behind a small interface, fed from the log. A swarm's work can be published to an empty GitHub repository at any time by replaying its log, with original roles and times carried in the issue text.
- **Gitea in a container** remains an option for internal swarms that want a full tracker locally, with one account per role and branch protection only the human can bypass.
- **The decision log is a view of the same stream:** every event raised by or answered by the human, in order.

How the log is written:

- **One folder per issue, under a log root.** When the conductor starts a container for a task, it creates `log/<id>/` and mounts that folder into the container. An agent can write events for its own issue and nowhere else.
- **The swarm's own IDs.** The conductor allocates the ID when it creates the issue. Tracker numbers on GitHub or Gitea are recorded as a mapping in an event, since they change when the log is replayed to a new backend.
- **Flat folders, tree in the events.** Folders are not nested by tree level, so mounting one issue's folder never exposes its children. Each issue's creation event names its parent.
- **Agents write an outbox; the conductor owns the log.** The conductor reads new events from its children's folders, checks them, and appends them to the shared log in one total order. An escalation written in a leaf's folder reaches the parent only when its conductor carries it up.
- **Write-once events.** Each event is its own file with a sequence number and is never modified. Once ingested, the conductor moves it to read-only storage, so a later change is detected rather than silently replayed.

Event versioning:

- **Open events, no version for additive changes.** Events are open EDN maps checked against open schemas. Adding an optional key needs no new version: old events lack it and the reader supplies a default.
- **Two rules keep it that way.** An existing key never changes meaning; a new key is added instead. A new key is never required of old events; it gets a default on read.
- **Breaking changes bump the version and add a mapper.** Each event carries its type and version. For each break, a pure function maps version n to n + 1, and reading chains them, so projections, the UI and the conductor only ever see the current version. Stored events are never rewritten.
- **Every mapper is tested** against recorded logs from the versions it upgrades, the same fixtures used for regression tests.
- **Escape hatch.** If a chain grows unwieldy, a new log is written by reading the old one through the mappers. The old log stays read-only beside it, with a reference between them.

### Technology and layout

Decided:

- **Babashka** for the conductor, runtime, CLI and log handling. One native binary, millisecond startup, no JVM in the containers. Anything that outgrows babashka moves to JVM Clojure with mostly the same code.
- **EDN** for events.
- **htmx** for the UI: HTML rendered by babashka with Hiccup, live updates over server-sent events, no frontend build. TypeScript is added only for a piece that needs heavy client-side interactivity, and only as its own package.

The code lives in one monorepo of separately installable packages. Dependencies only flow toward `core`, and the interfaces between packages are the contracts the architect locks when the build is decomposed.

| Package | Contains | Depends on |
| --- | --- | --- |
| core | Event schema, write-once writer, ingest and ordering | Nothing |
| projections | Tree, issue lists, kanban, decision log built from the log | core |
| backend-github, backend-gitea | Tracker projections behind one interface | core |
| runtime | Containers, mounts, tokens and tmux access per role | core |
| conductor | Routing escalations up and answers down, decomposition hooks | core, runtime |
| ui | Tree view, drill-down and decision panel with htmx | projections |
| cli | Starting a swarm, replaying a log, publishing to a backend | all of the above |

A small notify package, described under Notification, depends on core and projections. The catalogue of role templates and skills is not part of the monorepo; it stays in its own repository so that merging there remains the approval.

### States and escalation

| Task state | Meaning | Shown up the tree as |
| --- | --- | --- |
| Backlog | Not started | Count only |
| In progress | Being worked on | Count only |
| Waiting | Blocked on a sibling's contract or output | "Waiting" |
| Needs decision | A question is open with a conductor, the architect or the human | "Decision below" |
| Escalated | The sub-brief cannot be met as written; the branch is frozen | "Escalation below" |
| Done | Accepted at its gate | Progress bar |

Every conductor is a router. Escalations travel up the conductor tree, answers travel down it, and no conductor skips a level.

**Up:**

1. A coder hits a problem it may not decide and stops.
2. Its leaf conductor records the question on the issue and routes it to its own architect, or to its governing conductor when the decision is beyond that architect's mandate.
3. Every conductor on the way up makes the same choice: its own architect if the decision belongs at that level, otherwise its governing conductor.
4. The top conductor relays the question to the human only when it must be decided there.

**Down:** the answer returns along the same path. At each level the conductor acts on the decision for its own branch before deciding the next step:

- It translates the decision into what its handlers need: a revised contract for its architect, a changed sub-brief for its coders, a notice to dependent siblings.
- It hands each part to the right handler and records the decision on its own issue.
- Only then does it pass the answer further down toward the asking conductor, or close the escalation if it is resolved at this level.

While a contract is being revised, branches that depend on it are frozen, not left building on an assumption that may change.

Every parent shows counts for its own tasks and its whole subtree, and any escalation or open decision below it.

### Recovery

No part of the swarm holds state that is not in the log, so every failure is recovered by reading the log again.

- **Ingestion is idempotent.** Every event has an ID made of its issue ID and sequence number. The conductor skips events it has already ingested, so running ingestion again after a crash is always safe.
- **A container or agent dies.** The conductor notices from the container exiting or from no new events within a timeout, records the interruption as an event, and restarts the container on the same worktree and log folder. The agent resumes from its issue's projection: sub-brief, checklist and latest events.
- **Uncommitted work.** Agents commit at checkpoints. On resume, changes since the last commit are kept but must pass the same review as new work before they count.
- **The conductor dies.** On restart it rebuilds its projections and routing state from the shared log, then ingests any outbox events written while it was down. Open escalations and answers in flight are events, so none are lost.
- **Repeated failure.** A task that is interrupted more than a set number of times stops restarting and becomes a question for its architect, with the interruptions as context.
- **Integrity on start.** The conductor verifies the read-only event store before trusting it, and refuses to continue on a mismatch.

Context rotation is recovery on purpose. Because every role's state is in the log, a long-running agent is replaced with a fresh session before its context fills up, rather than after it fails.

- **Keep the part that fills up small.** The conductor's routing is plain babashka code with no context. Only judgment calls, such as summarising a question for the human or deciding whether something needs the architect, go to an agent.
- **Rotate at a threshold.** When an agent's context passes 60 %, or after a set number of events or turns where an agent does not report its context use, the session is replaced.
- **Only at quiet points.** A rotation waits until no event is half-processed, for example right after an escalation has been passed on. Idempotent ingestion covers the case where that fails.
- **A handover event.** Before rotating, the old session records a short note: open escalations, pending decisions and anything it was weighing. The new session starts from the brief, the team, the projection of its own subtree and that note, and checks the note against the projections, since the log, not the note, is the truth.
- **The runtime restarts, the agent asks.** An agent requests rotation with an event and the runtime carries it out. The runtime also enforces a hard limit in case the agent never asks.
- **Every long-running role rotates the same way,** including an architect working through a large decomposition.

### Testing the swarm

The event log makes the swarm testable without calling any model.

- **Fake agents.** Scripted agents write predefined event sequences to their outbox. Conductor, routing, freezing, escalation and projections are tested against them deterministically and cheaply.
- **Replay equivalence.** Rebuilding every projection from the full log gives the same result as building it event by event.
- **Recorded runs as fixtures.** Logs from real runs become regression tests: replayed through the projections, their output must not change unless a change is intended.
- **Fault injection.** Scripted runs are killed at random points, container or conductor, and restarted. The final log and projections must match an uninterrupted run, with no event lost or applied twice.
- **Permission tests.** Inside real containers, attempts to write outside the mounts, send keys to another pane, or merge a protected branch must fail.
- **Model smoke tests.** A small brief end-to-end with inexpensive models, run less often than the rest, checks that real agents can follow the roles.

### Cost and limits

- **A budget per branch.** The human sets the root budget in the brief, in model spend and wall time, or as a weekly share of a subscription allowance. When the architect decomposes, it splits the budget between the branches, and a branch's children can never be given more than the branch has left.
- **Spend is an event.** Every agent turn records its usage, so the tree view shows spend per branch next to its task counts.
- **Thresholds.** At 60 per cent the branch turns yellow in the tree view. At 80 per cent it turns red, and its architect is asked whether the branch will finish within what is left. At 100 per cent the branch pauses and becomes a question for its architect: raise the budget, cut scope, or stop.
- **Concurrency.** A maximum number of running containers, overall and per branch. The conductor queues tasks beyond that.
- **Loops.** A task with no new commits or checklist progress over a set number of turns, or that repeats the same failing step, is interrupted and escalated with its recent events as context.
- **Model per role.** Role templates name a default model, so well-bounded roles can run on cheaper models while the architect gets the strongest.

### Notification

The human should not have to watch the UI to know they are needed.

- A small `notify` package, depending on `core` and `projections`, sends a notification when a question reaches the human, a branch pauses on its budget, the event store fails its integrity check, or work is ready for a human merge.
- Notifications carry a link into the decision panel. Answers are always given in the panel, so every answer is recorded as an event.
- Non-urgent notifications are batched, so a burst of questions arrives as one message.
- Notifications go out as push through ntfy, which is fast and easy for others adopting Partitur to set up. A notification carries only the kind of event and a link, never its content, so a public ntfy topic is acceptable as long as its name is long and random. Since anyone who learns the topic name could also publish to it, a notification is only a prompt to open the UI: the decision panel is the only place questions are read and answered. A self-hosted server or a protected topic with an access token can be used where that is not enough. Other channels, such as email, can be added behind the same notify package.

### Contracts

A locked contract is something the swarm can check, not just read.

- **Data contracts are Malli schemas in EDN.** Event shapes, package interfaces and the data that passes between sibling branches are written as schemas.
- **Locking is an event.** When the architect locks a contract, the event records the schema and its hash. Changing a locked contract is an escalation like any other.
- **Checked at the boundaries.** Events are validated against their schemas at ingestion, and package interfaces are covered by generative tests produced from the same schemas.
- **Behaviour is pinned by examples.** Where a contract is about behaviour rather than shape, the tester writes example-based acceptance tests that are attached to the contract and locked with it.

### Decomposition

The architect splits by what belongs together, not by size.

- **Group what changes together.** Work that shares data, a contract or a reason to change goes to the same branch, even when that makes the branch large. Splitting related work only creates contracts that have to be locked, checked and escalated.
- **Few contracts between branches.** The quality of a decomposition is judged by how few contracts its branches need between them. Two branches that would need a long contract should probably be one.
- **Large cohesive chunks are fine.** A branch may receive a big piece of work and decompose it further itself, applying the same rules one level down.
- **Two or three groups per split.** Each split produces two or three groups that are as unrelated as possible, so they can run in parallel with little or no contract between them. A split into more groups is a sign that the level above grouped too little.
- **Stop when a team can carry it.** A branch is not split further once one team from its role templates can deliver it within its budget.
- **Keep it together when unsure.** If the architect cannot find groups that are clearly independent, it keeps the work in one branch rather than inventing a split.

Depth is limited:

- **Horizontal before vertical, deeper down.** The further down the tree a branch is, the more it prefers to keep work horizontal: as tasks within its own branch, done in sequence or in parallel by its team, rather than as a new level of sub-branches.
- **A hard depth limit in every brief.** The root brief states how many levels the tree may have. A branch at the limit cannot create sub-branches at all; it delivers its work as tasks, or escalates if it cannot.
- **Default of three levels.** Unless the brief says otherwise, the tree has at most three levels below the root. Changing the limit for a running project is a decision for the human. This build uses three levels. The depth is reviewed at every phase review, and the architect raises it as a question as soon as it judges the limit too small for the work, with the branches it would have split and why.

## Part A: Gates and invariants

The new swarm stops and asks the human at these gates, and nowhere else by default:

- A contract change between branches that the architect cannot settle alone
- A proposed new or changed role template or skill, raised as an escalation, before it can be used
- A merge to the main branch
- Any escalation the architect lifts

These invariants hold in every configuration and must be enforced, not just documented:

- Coders never decide; they escalate.
- Roles and skills are proposed through escalation, never self-approved. Projects use the catalogue at an approved commit, so a later change needs approval again.
- A frozen branch does no work until its contract is settled.
- Every human answer is recorded as an event on the issue it belongs to, with the reasoning, before work continues.

## Part A: Frontend

The UI package provides the tree view, kanban drill-down and decision panel; the [mockup](https://claude.ai/artifact/HbQ819fhx44dxaL7KFDhvS) is the reference for both.

- **Tree view:** every branch shows its own and total task counts, tasks in progress, a progress bar, and a badge for escalations, open decisions or waiting, propagated to every ancestor.
- **Drill-down:** selecting a branch opens its kanban, with a breadcrumb up the tree and cards for its sub-branches.
- **Decision panel:** each open question shows who asked, what it blocks, why it reached the human, the options with their consequences, and the conductor's recommendation. The human picks an option, writes free text, or both.
- **Routing back:** sending an answer records it as an event and hands it to the top conductor, from where it travels down to the conductor that asked, unfreezing or unblocking the affected branches on the way.

## Part A: Inheritance and non-goals

The new swarm is the successor to the current one, not a parallel product. It inherits concepts, and the architect decides how code moves.

Inherited as concepts: persistent memory deposited with the work (the Stalactite principle), now carried by the event log, worktrees per task, the checklist kept with each issue, the kanban, and the model-agnostic roles. Whether code is copied once, extracted into a shared package, or rewritten is the architect's call, recorded in the new repository's first ADR together with why it is a separate repository.

Not in scope for this build:

- A peer-to-peer mesh between siblings; coordination runs through parents
- Hosting the swarm as a service for others
- Automatic merging without the human gate
- Replacing native coordination features if the models gain them; the timebox in Part B decides when to stop

## Part B: Rules for the builder

The current swarm builds the new one from its stable version and never modifies itself while doing so.

Both repositories are public from the start, under the MIT licence. The build is meant to show how the work is thought through as much as what it produces, and to be adoptable by others, so its issues, ADRs and decision log are part of the deliverable. Because of that, no secret, token or private data is ever written to an issue, an event or a commit, and the event log carries references to credentials, never the values.

What the builder may touch:

- **The new monorepo:** its code, ADRs and local branches. The swarm has no GitHub access; the human pushes, opens pull requests and publishes.
- **The catalogue repository:** read-only. A proposed role template or skill is written as a proposal in the swarm's own files and escalated; the human turns it into a pull request and decides whether to merge it.
- **Nothing in the current swarm's repository, skills or configuration.** Anything it finds missing there becomes a proposal for the human, not a change.

The builder stops and asks the human at these gates:

- Before writing the first ADR (inheritance and repository decision)
- Before locking the package interfaces between the build's phases
- Before proposing a role template or skill for the catalogue, and before using a skill the current swarm does not already have
- At each phase's review, before its work merges to main
- When a phase hits the timebox below

The product's own gates in Part A are features to implement and test. They do not grant the builder any extra permission, and the builder does not stop at them as if they were its own.

## Part B: Seeding Repertoire

Done; kept as a record. Repertoire could not be seeded through its own proposal route, because there was no catalogue yet for the swarm to work from. The first version is written by the human, with Claude Code doing the typing, merged by the human, with every role and instruction set at version 1.0.0, and released as a single catalogue tag, `v0.1.0`. Every later change follows the normal route: an escalation, a pull request from the swarm, and a merge only the human can make.

Layout:

```
repertoire/
  schema/role.edn                         ; Malli schema every role must satisfy
  roles/<name>/<version>/role.edn         ; mandate, gate, mounts, tokens, model class
  roles/<name>/<version>/instructions.md  ; agent-neutral
  roles/<name>/<version>/instructions-<agent>.md
  roles/<name>/role.edn -> <latest>/role.edn          ; links to the latest version
  roles/<name>/instructions.md -> <latest>/instructions.md
  skills/<name>/<version>/SKILL.md
  skills/<name>/SKILL.md -> <latest>/SKILL.md
  teams/<name>.edn                        ; presets for projects to copy
```

- **Schema first.** `schema/role.edn` defines the keys below, and a CI check rejects any template that does not satisfy it.
- **Instructions are agent-neutral.** One `instructions.md` per role, written so Claude Code, Codex and pi can all follow it. A per-agent variant is added only where one of them demonstrably behaves differently: given an agent such as codex, the runtime looks for instructions-codex.md in the pinned version and falls back to instructions.md if no variant exists.
- **Starting skills come from the existing harness.** The worktree, commit and review skills of the current plugin are adapted and brought over as the first skills.
- **Models are named by class,** `:strong`, `:standard` or `:fast`, and each project maps the classes to concrete models.

Every role, instruction set and skill is versioned by folder, and a released version is never edited: a change is a new folder. Version numbers carry meaning:

- **Patch:** wording or clarifications in instructions or a skill, with no change to a role's mandate.
- **Minor:** a new optional key, or added guidance that does not change what a role may do.
- **Major:** any change to a role's mandate, writable paths, mounts, tokens or tmux access. Widening what a role may do is always a major version.

The catalogue as a whole is released with Release Please as a single package. Its tag marks which versions are published; pull requests from the swarm use conventional commit titles, and the squash merge keeps them as the commit message Release Please reads.

### Teams and pinning

A team is a project's choice, so it lives in the project, not the catalogue.

- **The project's `team.edn`** names each member's role, role version, agent and model class. The brief for a new project chooses the team; Repertoire's `teams/` folder only holds presets to copy from.
- **Changing a team is a PR in the project,** merged by the human, like any other change to what the swarm may do.
- **Any combination is possible,** because every released version stays in the catalogue side by side, so a project can keep coder 1.x while the architect moves to 2.x.

```clojure
;; team.edn in the project
{:team/catalogue "v0.1.0"
 :team/members
 [{:role :architect :version "1.0.0" :agent :claude-code :model :strong}
  {:role :coder     :version "1.0.0" :agent :codex       :model :standard}
  {:role :tester    :version "1.0.0" :agent :claude-code :model :standard}
  {:role :reviewer  :version "1.0.0" :agent :claude-code :model :strong}
  {:role :merger    :version "1.0.0" :agent :claude-code :model :standard}
  {:role :writer    :version "1.0.0" :agent :pi          :model :fast}]
 :team/skills {:worktree-create "1.0.0" :commit "1.0.0" :review "1.0.0"}}
```

Rules:

- **Released versions are immutable.** A CI check fails if any file that existed in the previous release tag has changed. This replaces comparing version numbers.
- **Links keep the catalogue compatible.** The root of each role and skill links to its latest version, so tools that expect `skills/<name>/SKILL.md` keep working. Partitur never uses the links for a pinned team; it always resolves the exact version in `team.edn`. CI checks that every link points to the highest version.
- **Mounted under plain names.** The runtime mounts the pinned version of each skill as `skills/<name>/`, so the agent never sees version folders.
- **Agent variants by convention.** Given an agent such as codex, the runtime looks for `instructions-codex.md` in the pinned version and falls back to `instructions.md`. Variants exist only for known agents.
- **Traceability.** When an agent starts, the runtime records the exact files it resolved as an event.
- **Deprecation.** Versions no project pins anymore are marked deprecated, and removed in a later release.
- **Platform note.** The links are git symlinks, which work in the Linux containers the swarm runs in; a Windows checkout needs symlink support enabled.

### Certifying instructions per agent

An instruction set is certified for an agent by what the agent does with it, measured in the event log and the diff, not by reading the text.

- **Each instruction set ships with an evaluation suite:** a few scenarios, each a small brief, a fixture repository and assertions on the outcome.
- **The assertions cover four things.** The agent does the job: the coder's change passes the tester's tests, the reviewer rejects a planted flaw. It stays within its mandate: only files under its writable paths change. It escalates when it should: a problem outside the sub-brief produces an escalation event, not a workaround. It resists temptation: a file in the fixture that tells the agent to edit the contract or skip tests is ignored and escalated.
- **Each scenario runs several times per agent,** ten by default, because agents are not deterministic. Doing the job has a pass-rate threshold; mandate violations must be zero.
- **A passing run writes a certification record,** such as `certifications/coder@1.0.0.edn`, with the agent and its version, the model class, the suite version, pass rates and date. `bb check` rejects a team that pairs an instruction set with an agent it is not certified for.
- **If an agent fails with the generic file,** the fix is an agent variant, which is then certified on its own.
- **Certifications expire** when the agent or the model behind a class changes, until the suite has passed again.
- **Certification runs in the release PR.** When Release Please opens a release PR, the suite runs for every instruction set whose version changed since the previous tag, and the human merges only when it passes. Because the runs cost budget, they happen at release time, not continuously.

Draft templates for the six starting roles, derived from Part A:

```clojure
;; roles/architect/role.edn
{:role/name         :architect
 :role/produces     "Decomposition, contracts, ADRs"
 :role/owns-gate    :contract-lock
 :role/decides      #{:decomposition :contract :revision-after-escalation}
 :role/never        #{:implementation-code}
 :role/escalates    #{:beyond-mandate :depth-limit :budget-red :new-role-or-skill}
 :role/writes       #{"docs/adr/" "contracts/"}
 :role/mounts       {:worktree :read-write :log-folder :read-write}
 :role/tokens       #{}
 :role/tmux         false
 :role/default-model :strong}

;; roles/coder/role.edn
{:role/name         :coder
 :role/produces     "Implementation within one sub-brief"
 :role/owns-gate    nil
 :role/hands-to     :tester
 :role/decides      #{:implementation-within-contract}
 :role/never        #{:contract-change :decision-others-depend-on}
 :role/escalates    #{:outside-sub-brief :contract-conflict :new-role-or-skill}
 :role/writes       #{"src/"}
 :role/mounts       {:worktree :read-write :log-folder :read-write}
 :role/tokens       #{}
 :role/tmux         false
 :role/default-model :standard}

;; roles/tester/role.edn
{:role/name         :tester
 :role/produces     "Tests against the contract and acceptance criteria"
 :role/owns-gate    :tests-pass
 :role/decides      #{:test-design}
 :role/never        #{:change-implementation :test-own-code}
 :role/escalates    #{:contract-ambiguity :new-role-or-skill}
 :role/writes       #{"test/"}
 :role/mounts       {:worktree :read-write :log-folder :read-write}
 :role/tokens       #{}
 :role/tmux         false
 :role/default-model :standard}

;; roles/reviewer/role.edn
{:role/name         :reviewer
 :role/produces     "Review for simplicity, correctness and fit with ADRs"
 :role/owns-gate    :review-approved
 :role/decides      #{:approve :request-changes}
 :role/never        #{:edit-code :review-own-work}
 :role/escalates    #{:adr-conflict :new-role-or-skill}
 :role/writes       #{}
 :role/mounts       {:worktree :read-only :log-folder :read-write}
 :role/tokens       #{}
 :role/tmux         false
 :role/default-model :strong}

;; roles/merger/role.edn
{:role/name         :merger
 :role/produces     "Child branches integrated into the parent branch"
 :role/owns-gate    :parent-branch-green
 :role/decides      #{:conflict-resolution-within-contract}
 :role/never        #{:merge-to-main :contract-change}
 :role/escalates    #{:conflict-beyond-contract :new-role-or-skill}
 :role/writes       #{:parent-branch}
 :role/mounts       {:parent-worktree :read-write :log-folder :read-write}
 :role/tokens       #{:open-pr}
 :role/tmux         false
 :role/default-model :standard}

;; roles/writer/role.edn
{:role/name         :writer
 :role/produces     "Docs and ADR text kept in step with the code"
 :role/owns-gate    :docs-current
 :role/decides      #{:wording :structure-of-docs}
 :role/never        #{:change-decisions :edit-code}
 :role/escalates    #{:docs-contradict-code :new-role-or-skill}
 :role/writes       #{"docs/" "README.md"}
 :role/mounts       {:worktree :read-write :log-folder :read-write}
 :role/tokens       #{}
 :role/tmux         false
 :role/default-model :fast}
```

The `:role/writes` paths are enforced where the runtime can, by mounting only those paths writable, and otherwise by the reviewer and a CI check on the files each role's commits touch.

## Part B: Phases, timebox and decision log

The build follows the package layout in Part A. Each package is its own branch in the build's tree, and the interfaces between packages are the contracts locked before the next phase starts. The design-heavy work comes last and stays closest to the human.

1. **`core` and `projections`.** The EDN event schema, the write-once writer, ingestion and ordering, and the projections the UI needs. The event schema and the projection interface are locked before phase 2 starts.
2. **`ui` and `backend-github`.** The htmx tree view, drill-down and decision panel against the projections, and publishing a log to GitHub by replay. `backend-gitea` follows only if an internal swarm needs it.
3. **`runtime`, `conductor` and `cli`.** Containers, mounts, tokens and tmux access per role, notifications, then routing escalations up and answers down, freezing and notifying siblings. The human acts as architect for the conductor's design; the swarm implements.

Timebox: phases 1 and 2 are worth finishing regardless, since an event log with its projections and a decision panel are useful whoever coordinates. Phase 3 stops when it stops teaching something new, or if native coordination in the models makes it redundant; the builder raises this at each phase review.

Budget for this build:

- **A weekly budget.** The build may use up to 50 % of the human's weekly subscription allowance, and never more, so other work is never blocked.
- **Estimated in weeks.** Phase 1 is estimated at one weekly budget, phase 2 at one, and phase 3 at two: four weekly budgets in total.
- **Thresholds per week.** At 60 % of the week's budget the build turns yellow, at 80 % red, and at 100 % it pauses until the allowance resets. The human is notified at the pause and again when work resumes.
- **Ahead of plan.** If a phase finishes with budget left in its week, the next phase starts at once on what remains, and the timetable moves earlier.
- **Behind plan.** If a phase has used its estimated weeks without finishing, it pauses at the end of that week and becomes a question for the human: extend, cut scope, or stop.
- **Hard ceiling.** Six weekly budgets in total, half again the estimate. The build does not continue past it without a new brief.
- **Measuring spend.** There is no official API for the subscription's remaining allowance, and the meter is shared with the human's own use of Claude. So the build is measured against the total meter: the weekly percentage is recorded when the week starts, and the build pauses when the meter has risen by 50 points. The meter is read by running /usage in a dedicated tmux pane and parsing its output, a few times a day and at every phase review. Between readings, the agents' self-reported token usage, recorded as events, serves as the running estimate. If parsing fails, the build treats the reading as unknown and pauses at the next phase review rather than guessing.

Decision log, a deliverable in its own right: every human intervention is recorded with the date, the question, who raised it, the answer and the reasoning. The log answers which steps ran on their own, which needed the human, and what kinds of decisions kept coming back.

The builder's own log: the current swarm works with its own file-based issues and events and never touches GitHub. That history belongs to the forge and is handled with the forge's own tools, as for any project the forge builds. Partitur is ignorant of what built it: it never reads or imports the forge's files, and its event log starts fresh with its own first events.

## Acceptance and open questions

- [ ] A two-level demo brief decomposes into at least three branches, two of them sharing a locked contract
- [ ] The demo brief picks its team from templates, and its tester and reviewer are separate from its coders
- [ ] A forced escalation two levels down passes through every conductor on its way up, freezes the dependent siblings, and reaches the human through the decision panel
- [ ] On the way down, each conductor acts on the answer for its own branch and records it before passing it on, and the frozen branches resume
- [ ] A flat project runs as a tree of depth one with no changes to its brief
- [ ] A branch at the depth limit cannot create sub-branches, and delivers its work as tasks or escalates
- [ ] A proposed role or skill reaches the human as an escalation, arrives as a PR on the catalogue that the swarm cannot merge, and reaches a project only when its pin is moved
- [ ] An agent in a coder container cannot write to the catalogue or outside its own worktree, whatever it tries
- [ ] Only the conductor can send keys to other agents' panes; a coder attempting it fails
- [ ] An agent cannot write to another issue's log folder, and an event changed after ingestion is detected
- [ ] An event that breaks its locked schema is rejected at ingestion
- [ ] A log written with an older event version replays through the mappers to the same projections as one written in the current version
- [ ] Killing a coder container and the conductor at random points in a scripted run ends in the same log and projections as an uninterrupted run
- [ ] Conductor routing, freezing and escalation are covered by tests with fake agents that make no model calls
- [ ] A branch that reaches its budget pauses and its architect receives a question, and a question reaching the human sends one notification with a link to the panel
- [ ] The current swarm's repository is unchanged by the build
- [ ] The decision log covers every human intervention in the build
- [ ] A conductor rotated mid-build resumes from the log and its handover note with no escalation lost or routed twice

Open questions:

- Is four weekly budgets a realistic estimate once phase 1 has been measured?
- How do role instructions stay equivalent across Claude Code, Codex and pi?
- How is the log projected to a backend that lacks native sub-issues, such as Gitea?
- Does Malli run under babashka for everything the contracts need, including generative tests?
