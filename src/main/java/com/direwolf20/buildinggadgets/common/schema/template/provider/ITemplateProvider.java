package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.common.schema.template.Template;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;

public interface ITemplateProvider {

    /**
     * Attempts to retrieve the current {@link Template} if already present. May query the Server if the
     * {@link Template} is not present, but does not wait for the result.
     *
     * @param uuid The id for which to retrieve a
     * @return An {@link Optional} containing the {@link Template} corresponding to {@code id} if present or
     *         {@link Optional#empty()} else.
     */
    Optional<Template> getIfAvailable(UUID uuid); //Paste-Render

    /**
     * Register a callback to be called when the {@link Template} known by {@code id} is updated. The callback should not
     * be triggered when a corresponding cache entry is triggered. Listeners may time out and in this case will be invoked
     * with the current {@link Template} value and a value of true for the second parameter. Returning true instead of false
     * will be prevent the listener from being removed.
     *
     * @param id       The id of the {@link Template} to query
     * @param callback Callback to invoke as soon as the Template changes. May be {@link Optional#empty()} if the template
     *                 is removed by an explicit call to {@link #setAndUpdateRemote(UUID, Template, PacketTarget)}. Returns
     *                 {@code true} if it wishes to remain registered.
     */
    void registerUpdateCallback(UUID id, BiPredicate<Optional<Template>, Boolean> callback);//TemplateManager wants to copy a Template

    /**
     * Set's the {@link Template} corresponding to {@code id} to the given value and updates the other side if specified.
     * Notice that this will trigger callbacks added to {@link #registerUpdateCallback(UUID, BiPredicate)}.
     *
     * @param id         The id of the {@link Template} which is to be updated
     * @param template   The new {@link Template}
     * @param sendTarget The {@link PacketTarget} to which an update is to be send to. If null no update will be send.
     */
    //Server Copies building or Template Manager copied a template from client clipboard
    void setAndUpdateRemote(UUID id, @Nullable Template template, @Nullable PacketTarget sendTarget);
}
