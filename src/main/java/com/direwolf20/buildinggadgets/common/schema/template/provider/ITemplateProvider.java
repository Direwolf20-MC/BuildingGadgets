package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.common.schema.template.Template;
import jdk.internal.jline.internal.Nullable;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface ITemplateProvider {

    /**
     * Attempts to retrieve the current {@link Template} if already present. Does not query the other side if the
     * {@link Template} is not present.
     *
     * @param uuid The id for which to retrieve a
     * @return An {@link Optional} containing the {@link Template} corresponding to {@code id} if present or
     * {@link Optional#empty()} else.
     */
    Optional<Template> getIfAvailable(UUID uuid); //Paste-Render

    /**
     * Retrieves the most recent version of the Server {@link Template} as soon as available.
     * <p>
     * On the Server the callback will directly invoked with the return value of {@link #getIfAvailable(UUID)}
     * whilst on the Client this results in querying the Server for the current {@link Template} and then invoking the
     * callback once this query arrives.
     *
     * @param id       The id of the {@link Template} to query
     * @param callback callback to invoke as soon as the Template is ready. May be {@link Optional#empty()} if the
     *                 Server has no {@link Template} for {@code id}
     */
    void getWhenAvailable(UUID id, Consumer<Optional<Template>> callback);//TemplateManager wants to copy a Template

    /**
     * Set's the {@link Template} corresponding to {@code id} to the given value and updates the other side if specified.
     *
     * @param id         The id of the {@link Template} which is to be updated
     * @param template   The new {@link Template}
     * @param sendTarget The {@link PacketTarget} to which an update is to be send to. If null no update will be send.
     */
    //Server Copies building or Template Manager copied a template from client clipboard
    void setAndUpdateRemote(UUID id, Template template, @Nullable PacketTarget sendTarget);
}
