export const findById = (resources, id) => {
  if (!resources) return null;
  return resources.find((r) => r.id === id);
};

export const upsert = (resource, item) => {
  const index = resource.findIndex((r) => r.id === item.id);
  if (item.id && index !== -1) {
    resource[index] = item;
  } else {
    resource.push(item);
  }
};
