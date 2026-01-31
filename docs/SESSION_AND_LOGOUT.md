# Session Component and Logout — Disposing the Session Scope

This document explains how the **SessionComponent** custom scope works in the sample app and how **logout** disposes the session so that all session-scoped objects live until a new component is created.

---

## 1. Pattern: ViewModel owns logout (no CompositionLocal)

We **do not** use `CompositionLocalOf` to provide the session scope. That would be an anti-pattern: it hides the session container in implicit composition scope and makes disposal and testing harder.

Instead:

- A **ViewModel** (`SessionViewModel`) exposes **logout()** and **getSessionState()**.
- The session scope is held in a **holder** (`SessionHolder`) that the ViewModel delegates to.
- The UI gets the session ViewModel via **viewModelAnchor&lt;SessionViewModel&gt;()** and passes it down explicitly (e.g. to `ProductAppRoot`, then to `ProductListScreen`).
- When the user taps **Logout**, the UI calls **sessionViewModel.logout()**.

No CompositionLocal; the session API is explicit and testable.

---

## 2. What happens when you call logout()

When **logout()** is called:

1. The holder creates a **new** `Anchor.scopedContainer(SessionComponent::class)`.
2. The **previous** scoped container is no longer referenced; it becomes unreachable.
3. All objects that were in the **old** session scope (e.g. [SessionState], any type bound in [SessionComponent]) are no longer used; they can be garbage-collected.
4. The **next** call to **getSessionState()** (or any resolution from the session scope) uses the **new** container, so a new `SessionState` (and any other session-scoped type) is created.

So: **calling logout disposes the current session scope and creates a new one.**

---

## 3. Lifetime of session-scoped objects

**Whatever is bound in `SessionComponent` (e.g. `SessionState`) lives until a new component is created.**

- **Before logout:** One `SessionComponent` scoped container exists. All session-scoped instances (e.g. one `SessionState`) live in that container.
- **When logout() is called:** We create a new scoped container and replace the reference. The old container is no longer used.
- **After logout:** New requests (e.g. getSessionState()) resolve from the new container. Old instances are no longer referenced and can be collected.

So session-scoped objects **do not** live “forever”; they live **until logout()** (or until you otherwise replace the session container with a new one).

---

## 4. Summary

| Concept | Meaning |
|--------|--------|
| **No CompositionLocal** | Session is not provided via CompositionLocal; use a ViewModel with logout() and pass it down. |
| **logout()** | Creates a new `SessionComponent` scoped container; the previous scope is effectively disposed. |
| **Session-scoped objects** | Live until a new component is created (i.e. until logout() or equivalent). |

Use this pattern in your app: a ViewModel (or similar owner) that holds or delegates to the session scope and exposes **logout()**, and document that **logout** means “dispose current session scope and create a new one.”
