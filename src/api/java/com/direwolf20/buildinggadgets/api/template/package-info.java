/**
 * Contains the core template api with the {@link com.direwolf20.buildinggadgets.api.template.ITemplate} class.
 * <p>
 * Templates are 3D-structures which can be build in a certain context (see {@link com.direwolf20.buildinggadgets.api.template.building building}),
 * transformed in various ways (see {@link com.direwolf20.buildinggadgets.api.template.transaction transactions}) and serialized (see
 * {@link com.direwolf20.buildinggadgets.api.serialisation serialisation}). Currently there are 2 major implementations
 * {@link com.direwolf20.buildinggadgets.api.template.ImmutableTemplate} and {@link com.direwolf20.buildinggadgets.api.template.DelegatingTemplate}.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
package com.direwolf20.buildinggadgets.api.template;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;